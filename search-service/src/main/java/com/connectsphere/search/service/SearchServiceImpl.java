package com.connectsphere.search.service;

import com.connectsphere.search.dto.HashtagResponse;
import com.connectsphere.search.dto.IndexPostRequest;
import com.connectsphere.search.dto.IndexUserRequest;
import com.connectsphere.search.dto.SearchPostResponse;
import com.connectsphere.search.dto.SearchUserResponse;
import com.connectsphere.search.model.HashtagIndex;
import com.connectsphere.search.model.PostHashtag;
import com.connectsphere.search.model.SearchPost;
import com.connectsphere.search.model.SearchUser;
import com.connectsphere.search.repository.HashtagIndexRepository;
import com.connectsphere.search.repository.PostHashtagRepository;
import com.connectsphere.search.repository.SearchPostRepository;
import com.connectsphere.search.repository.SearchUserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchServiceImpl implements SearchService {
    private final SearchPostRepository searchPostRepository;
    private final SearchUserRepository searchUserRepository;
    private final HashtagIndexRepository hashtagIndexRepository;
    private final PostHashtagRepository postHashtagRepository;

    public SearchServiceImpl(
            SearchPostRepository searchPostRepository,
            SearchUserRepository searchUserRepository,
            HashtagIndexRepository hashtagIndexRepository,
            PostHashtagRepository postHashtagRepository
    ) {
        this.searchPostRepository = searchPostRepository;
        this.searchUserRepository = searchUserRepository;
        this.hashtagIndexRepository = hashtagIndexRepository;
        this.postHashtagRepository = postHashtagRepository;
    }

    @Override
    @Transactional
    public SearchPostResponse indexPost(IndexPostRequest request) {
        SearchPost searchPost = searchPostRepository.findById(request.postId()).orElseGet(SearchPost::new);
        searchPost.setPostId(request.postId());
        searchPost.setAuthorId(request.authorId());
        searchPost.setContent(request.content());

        SearchPost savedPost = searchPostRepository.save(searchPost);
        replacePostHashtags(request.postId(), request.content());
        return SearchPostResponse.from(savedPost);
    }

    @Override
    @Transactional
    public void removePostIndex(Long postId) {
        searchPostRepository.deleteById(postId);
        clearPostHashtags(postId);
    }

    @Override
    @Transactional
    public SearchUserResponse indexUser(IndexUserRequest request) {
        SearchUser searchUser = searchUserRepository.findById(request.userId()).orElseGet(SearchUser::new);
        searchUser.setUserId(request.userId());
        searchUser.setUsername(request.username());
        searchUser.setFullName(request.fullName());

        return SearchUserResponse.from(searchUserRepository.save(searchUser));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchPostResponse> searchPosts(String query) {
        return searchPostRepository.findByContentContainingIgnoreCaseOrderByIndexedAtDesc(query).stream()
                .map(SearchPostResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchUserResponse> searchUsers(String query) {
        return searchUserRepository
                .findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByIndexedAtDesc(query, query)
                .stream()
                .map(SearchUserResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> searchHashtags(String query) {
        return hashtagIndexRepository.findByTagContainingIgnoreCaseOrderByUsageCountDesc(query).stream()
                .map(HashtagResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashtagResponse> trendingHashtags(int limit) {
        int safeLimit = Math.max(1, limit);
        return hashtagIndexRepository.findAll().stream()
                .sorted((left, right) -> Long.compare(right.getUsageCount(), left.getUsageCount()))
                .limit(safeLimit)
                .map(HashtagResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getHashtagsForPost(Long postId) {
        return postHashtagRepository.findByPostId(postId).stream()
                .map(PostHashtag::getTag)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchPostResponse> getPostsByHashtag(String tag) {
        Set<Long> postIds = postHashtagRepository.findByTagOrderByCreatedAtDesc(normalizeTag(tag)).stream()
                .map(PostHashtag::getPostId)
                .collect(Collectors.toSet());
        return searchPostRepository.findAllById(postIds).stream()
                .map(SearchPostResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getHashtagCount(String tag) {
        return postHashtagRepository.countByTag(normalizeTag(tag));
    }

    private void replacePostHashtags(Long postId, String content) {
        clearPostHashtags(postId);

        extractTags(content).forEach(tag -> {
            increaseHashtagCount(tag);

            PostHashtag postHashtag = new PostHashtag();
            postHashtag.setPostId(postId);
            postHashtag.setTag(tag);
            postHashtagRepository.save(postHashtag);
        });
    }

    private void clearPostHashtags(Long postId) {
        for (PostHashtag postHashtag : postHashtagRepository.findByPostId(postId)) {
            decreaseHashtagCount(postHashtag.getTag());
        }
        postHashtagRepository.deleteByPostId(postId);
    }

    private List<String> extractTags(String content) {
        return Arrays.stream(content.split("\\s+"))
                .filter(word -> word.startsWith("#") && word.length() > 1)
                .map(this::normalizeTag)
                .distinct()
                .toList();
    }

    private String normalizeTag(String tag) {
        return tag.replaceAll("[^#A-Za-z0-9_]", "").toLowerCase(Locale.ROOT);
    }

    private void increaseHashtagCount(String tag) {
        HashtagIndex hashtagIndex = hashtagIndexRepository.findById(tag).orElseGet(HashtagIndex::new);
        hashtagIndex.setTag(tag);
        hashtagIndex.setUsageCount(hashtagIndex.getUsageCount() + 1);
        hashtagIndex.setLastUsedAt(Instant.now());
        hashtagIndexRepository.save(hashtagIndex);
    }

    private void decreaseHashtagCount(String tag) {
        hashtagIndexRepository.findById(tag).ifPresent(hashtagIndex -> {
            long updatedCount = Math.max(0, hashtagIndex.getUsageCount() - 1);
            if (updatedCount == 0) {
                hashtagIndexRepository.delete(hashtagIndex);
            } else {
                hashtagIndex.setUsageCount(updatedCount);
                hashtagIndexRepository.save(hashtagIndex);
            }
        });
    }
}
