package com.connectsphere.search.dto;

import com.connectsphere.search.model.HashtagIndex;
import java.time.Instant;

public record HashtagResponse(
        String tag,
        long usageCount,
        Instant lastUsedAt
) {
    public static HashtagResponse from(HashtagIndex hashtagIndex) {
        return new HashtagResponse(hashtagIndex.getTag(), hashtagIndex.getUsageCount(), hashtagIndex.getLastUsedAt());
    }
}
