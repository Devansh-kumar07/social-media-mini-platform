package com.connectsphere.social.service;

import com.connectsphere.social.client.NotificationServiceClient;
import com.connectsphere.social.dto.CommentResponse;
import com.connectsphere.social.dto.CreateCommentRequest;
import com.connectsphere.social.dto.UpdateCommentRequest;
import com.connectsphere.social.model.Comment;
import com.connectsphere.social.model.Post;
import com.connectsphere.social.repository.CommentRepository;
import com.connectsphere.social.repository.PostRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // ✅ NEW: Notification client to tell users when someone comments on their post
    private final NotificationServiceClient notificationServiceClient;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              NotificationServiceClient notificationServiceClient) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long userId, Long postId, CreateCommentRequest request) {
        Post post = findPost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setContent(request.content());

        if (request.parentCommentId() != null) {
            Comment parent = findCommentEntity(request.parentCommentId());
            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("Reply must belong to the same post");
            }
            if (parent.getParentCommentId() != null) {
                throw new IllegalArgumentException("Only one reply level is allowed");
            }
            comment.setParentCommentId(request.parentCommentId());
        }

        Comment savedComment = commentRepository.save(comment);
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        // ✅ INTEGRATION: Notify post author that someone commented
        notificationServiceClient.sendNotification(
                post.getAuthorId(),      // post author gets notified
                userId,                 // the commenter
                "COMMENT",
                "Someone commented on your post"
        );

        // ✅ INTEGRATION: If this is a reply, also notify the parent comment's author
        if (request.parentCommentId() != null) {
            Comment parentComment = findCommentEntity(request.parentCommentId());
            // Only notify parent comment author if they're a different person from post author
            // (to avoid double notification)
            if (!parentComment.getAuthorId().equals(post.getAuthorId())) {
                notificationServiceClient.sendNotification(
                        parentComment.getAuthorId(),   // reply goes to parent comment author
                        userId,
                        "REPLY",
                        "Someone replied to your comment"
                );
            }
        }

        return mapComment(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId) {
        return mapComment(findCommentEntity(commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUser(Long authorId, Pageable pageable) {
        return commentRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(authorId, pageable)
                .map(this::mapComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId).stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .map(this::mapComment)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request) {
        Comment comment = findCommentEntity(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can update this comment");
        }
        comment.setContent(request.content());
        return mapComment(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findCommentEntity(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can delete this comment");
        }
        softDeleteComment(comment);
    }

    @Override
    @Transactional
    public void adminDeleteComment(Long commentId, Long adminUserId) {
        Comment comment = findCommentEntity(commentId);
        softDeleteComment(comment);
        notificationServiceClient.sendNotification(
                comment.getAuthorId(),
                adminUserId,
                "WARNING",
                "Your comment was removed by admin for violating community guidelines",
                commentId,
                "COMMENT"
        );
    }

    private void softDeleteComment(Comment comment) {
        if (!comment.isDeleted()) {
            comment.setDeleted(true);
            commentRepository.save(comment);

            Post post = findPost(comment.getPostId());
            post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
            postRepository.save(post);
        }
    }

    private CommentResponse mapComment(Comment comment) {
        List<CommentResponse> replies = commentRepository.findByParentCommentIdAndDeletedFalseOrderByCreatedAtAsc(comment.getCommentId())
                .stream()
                .map(reply -> CommentResponse.from(reply, List.of()))
                .toList();
        return CommentResponse.from(comment, replies);
    }

    private Comment findCommentEntity(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Comment not found");
        }
        return comment;
    }

    private Post findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("Post not found");
        }
        return post;
    }
}
