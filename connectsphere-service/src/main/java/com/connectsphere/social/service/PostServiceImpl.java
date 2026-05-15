package com.connectsphere.social.service;

import com.connectsphere.social.client.AuthServiceClient;
import com.connectsphere.social.client.MediaServiceClient;
import com.connectsphere.social.client.NotificationServiceClient;
import com.connectsphere.social.client.SearchServiceClient;
import com.connectsphere.social.dto.AuthUserResponse;
import com.connectsphere.social.dto.CreatePostRequest;
import com.connectsphere.social.dto.PostResponse;
import com.connectsphere.social.dto.SocialStatsResponse;
import com.connectsphere.social.dto.UploadedMediaResponse;
import com.connectsphere.social.dto.UpdatePostRequest;
import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.PostVisibility;
import com.connectsphere.social.repository.CommentRepository;
import com.connectsphere.social.repository.ContentReportRepository;
import com.connectsphere.social.repository.FollowRepository;
import com.connectsphere.social.repository.PostRepository;
import com.connectsphere.social.repository.ReactionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostServiceImpl implements PostService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<!\\w)@([A-Za-z0-9_]{3,60})");

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final ContentReportRepository contentReportRepository;
    private final MediaServiceClient mediaServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final AuthServiceClient authServiceClient;

    // ✅ NEW: Search client so we can index posts for search
    private final SearchServiceClient searchServiceClient;

    public PostServiceImpl(
            PostRepository postRepository,
            FollowRepository followRepository,
            CommentRepository commentRepository,
            ReactionRepository reactionRepository,
            ContentReportRepository contentReportRepository,
            MediaServiceClient mediaServiceClient,
            NotificationServiceClient notificationServiceClient,
            AuthServiceClient authServiceClient,
            SearchServiceClient searchServiceClient
    ) {
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.contentReportRepository = contentReportRepository;
        this.mediaServiceClient = mediaServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.authServiceClient = authServiceClient;
        this.searchServiceClient = searchServiceClient;
    }

    @Override
    @Transactional
    public PostResponse createPost(Long authorId, CreatePostRequest request) {
        String content = normalizeContent(request.content());
        List<String> mediaUrls = sanitizeMediaUrls(request.mediaUrls());
        validatePostData(content, mediaUrls);

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);
        post.setVisibility(request.visibility() == null ? PostVisibility.PUBLIC : request.visibility());
        post.setMediaUrls(mediaUrls);
        Post savedPost = postRepository.save(post);

        // ✅ INTEGRATION: Tell search-service about this new post
        // So users can find it when they search keywords or hashtags
        searchServiceClient.indexPost(savedPost.getPostId(), authorId, searchableContent(savedPost.getContent()));
        notifyMentionedUsers(savedPost, authorId, null);

        return PostResponse.from(savedPost);
    }

    @Override
    @Transactional
    public PostResponse createPostWithMedia(Long authorId, String content, String visibility, MultipartFile[] files) {
        String normalizedContent = normalizeContent(content);
        List<MultipartFile> validFiles = sanitizeFiles(files);
        validatePostData(normalizedContent, validFiles.stream().map(MultipartFile::getOriginalFilename).toList());

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(normalizedContent);
        post.setVisibility(parseVisibility(visibility));
        post.setMediaUrls(List.of());

        Post savedPost = postRepository.save(post);

        List<String> mediaUrls = validFiles.stream()
                .map(file -> mediaServiceClient.uploadPostMedia(authorId, savedPost.getPostId(), file))
                .map(UploadedMediaResponse::downloadUrl)
                .toList();

        savedPost.setMediaUrls(mediaUrls);
        Post updatedPost = postRepository.save(savedPost);

        searchServiceClient.indexPost(updatedPost.getPostId(), authorId, searchableContent(updatedPost.getContent()));
        notifyMentionedUsers(updatedPost, authorId, null);

        return PostResponse.from(updatedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return PostResponse.from(findVisiblePost(postId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPublicFeed(Pageable pageable) {
        return postRepository.findByDeletedFalseAndVisibility(PostVisibility.PUBLIC, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(Long authorId, Pageable pageable) {
        return postRepository.findByDeletedFalseAndAuthorId(authorId, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedForUser(Long userId, Pageable pageable) {
        List<Long> followeeIds = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId).stream()
                .map(follow -> follow.getFolloweeId())
                .toList();

        if (followeeIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return postRepository.findByDeletedFalseAndAuthorIdIn(followeeIds, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        return postRepository.findByDeletedFalseAndContentContainingIgnoreCase(query, pageable)
                .map(PostResponse::from);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = findVisiblePost(postId);
        ensureAuthor(post, userId);

        if (request.content() != null) {
            post.setContent(normalizeContent(request.content()));
        }
        if (request.visibility() != null) {
            post.setVisibility(request.visibility());
        }
        if (request.mediaUrls() != null) {
            post.setMediaUrls(sanitizeMediaUrls(request.mediaUrls()));
        }

        validatePostData(post.getContent(), post.getMediaUrls());

        Post savedPost = postRepository.save(post);

        // ✅ INTEGRATION: Re-index updated post so search stays current
        searchServiceClient.indexPost(
                savedPost.getPostId(),
                savedPost.getAuthorId(),
                searchableContent(savedPost.getContent())
        );
        notifyMentionedUsers(savedPost, userId, post.getContent());

        return PostResponse.from(savedPost);
    }

    @Override
    @Transactional
    public PostResponse changeVisibility(Long postId, Long userId, UpdatePostRequest request) {
        Post post = findVisiblePost(postId);
        ensureAuthor(post, userId);
        if (request.visibility() == null) {
            throw new IllegalArgumentException("Visibility is required");
        }
        post.setVisibility(request.visibility());
        return PostResponse.from(postRepository.save(post));
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findVisiblePost(postId);
        ensureAuthor(post, userId);
        post.setDeleted(true);
        postRepository.save(post);

        // ✅ INTEGRATION: Remove deleted post from search index
        searchServiceClient.removePostFromIndex(postId);
    }

    @Override
    @Transactional
    public void adminDeletePost(Long postId, Long adminUserId) {
        Post post = findVisiblePost(postId);
        post.setDeleted(true);
        postRepository.save(post);

        // ✅ INTEGRATION: Also remove from search when admin deletes
        searchServiceClient.removePostFromIndex(postId);
        notificationServiceClient.sendNotification(
                post.getAuthorId(),
                adminUserId,
                "WARNING",
                "Your post was removed by admin for violating community guidelines",
                postId,
                "POST"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getPostCount(Long authorId) {
        return postRepository.countByDeletedFalseAndAuthorId(authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public SocialStatsResponse getStats() {
        return new SocialStatsResponse(
                postRepository.findAll().stream().filter(post -> !post.isDeleted()).count(),
                commentRepository.findAll().stream().filter(comment -> !comment.isDeleted()).count(),
                reactionRepository.count(),
                contentReportRepository.findByStatusOrderByCreatedAtDesc(com.connectsphere.social.model.ReportStatus.OPEN).size()
        );
    }

    private Post findVisiblePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("Post not found");
        }
        return post;
    }

    private void ensureAuthor(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can modify this post");
        }
    }

    private void validatePostData(String content, List<String> mediaUrls) {
        if (content == null && (mediaUrls == null || mediaUrls.isEmpty())) {
            throw new IllegalArgumentException("Post must contain text or media");
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String trimmedContent = content.trim();
        return trimmedContent.isEmpty() ? null : trimmedContent;
    }

    private List<String> sanitizeMediaUrls(List<String> mediaUrls) {
        if (mediaUrls == null) {
            return new ArrayList<>();
        }

        return mediaUrls.stream()
                .filter(url -> url != null && !url.trim().isEmpty())
                .map(String::trim)
                .toList();
    }

    private List<MultipartFile> sanitizeFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }

        return Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private PostVisibility parseVisibility(String visibility) {
        if (visibility == null || visibility.trim().isEmpty()) {
            return PostVisibility.PUBLIC;
        }

        try {
            return PostVisibility.valueOf(visibility.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid visibility value");
        }
    }

    private String searchableContent(String content) {
        return content == null ? "" : content;
    }

    private void notifyMentionedUsers(Post post, Long actorId, String previousContent) {
        Set<String> currentMentions = extractMentions(post.getContent());
        if (currentMentions.isEmpty()) {
            return;
        }

        Set<String> previousMentions = extractMentions(previousContent);
        AuthUserResponse actor = authServiceClient.getUserById(actorId);
        String actorName = actor != null && actor.fullName() != null && !actor.fullName().isBlank()
                ? actor.fullName()
                : actor != null && actor.username() != null && !actor.username().isBlank()
                ? actor.username()
                : "Someone";

        for (String username : currentMentions) {
            if (previousMentions.contains(username)) {
                continue;
            }

            AuthUserResponse mentionedUser = authServiceClient.getUserByUsername(username);
            if (mentionedUser == null || mentionedUser.userId() == null) {
                continue;
            }

            notificationServiceClient.sendNotification(
                    mentionedUser.userId(),
                    actorId,
                    "MENTION",
                    actorName + " mentioned you in a post",
                    post.getPostId(),
                    "POST"
            );
        }
    }

    private Set<String> extractMentions(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        Set<String> mentions = new LinkedHashSet<>();
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }
}
