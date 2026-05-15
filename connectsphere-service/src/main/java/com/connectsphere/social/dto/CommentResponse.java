package com.connectsphere.social.dto;

import com.connectsphere.social.model.Comment;
import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long authorId,
        Long parentCommentId,
        String content,
        long likesCount,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getLikesCount(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }
}
