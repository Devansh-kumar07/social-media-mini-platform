package com.connectsphere.social.dto;

import com.connectsphere.social.model.ReactionTargetType;
import com.connectsphere.social.model.ReactionType;
import jakarta.validation.constraints.NotNull;

public record CreateReactionRequest(
        @NotNull Long targetId,
        @NotNull ReactionTargetType targetType,
        @NotNull ReactionType reactionType
) {
}
