package com.connectsphere.social.dto;

import com.connectsphere.social.model.Post;
import com.connectsphere.social.model.PostVisibility;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record PostResponse(
        Long postId,
        Long authorId,
        String content,
        List<String> mediaUrls,
        PostVisibility visibility,
        long likesCount,
        long commentsCount,
        long sharesCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getPostId(),
                post.getAuthorId(),
                post.getContent(),
                post.getMediaUrls() == null ? List.of() : new ArrayList<>(post.getMediaUrls()),
                post.getVisibility(),
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getSharesCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
