package com.connectsphere.social.dto;

import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.PostVisibility;
import java.time.Instant;

public record PostResponse(
        Long postId,
        Long authorId,
        String content,
        PostVisibility visibility,
        long likesCount,
        long commentsCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getPostId(),
                post.getAuthorId(),
                post.getContent(),
                post.getVisibility(),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}

