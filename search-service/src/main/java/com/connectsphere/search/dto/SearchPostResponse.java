package com.connectsphere.search.dto;

import com.connectsphere.search.model.SearchPost;
import java.time.Instant;

public record SearchPostResponse(
        Long postId,
        Long authorId,
        String content,
        Instant indexedAt
) {
    public static SearchPostResponse from(SearchPost searchPost) {
        return new SearchPostResponse(
                searchPost.getPostId(),
                searchPost.getAuthorId(),
                searchPost.getContent(),
                searchPost.getIndexedAt()
        );
    }
}
