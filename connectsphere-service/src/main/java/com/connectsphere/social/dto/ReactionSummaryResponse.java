package com.connectsphere.social.dto;

import com.connectsphere.social.model.ReactionTargetType;
import java.util.Map;

public record ReactionSummaryResponse(
        Long targetId,
        ReactionTargetType targetType,
        long totalCount,
        Map<String, Long> reactionCounts
) {
}
