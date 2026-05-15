package com.connectsphere.social.dto;

import com.connectsphere.social.model.ReactionType;
import jakarta.validation.constraints.NotNull;

public record UpdateReactionRequest(
        @NotNull ReactionType reactionType
) {
}
