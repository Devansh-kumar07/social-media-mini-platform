package com.connectsphere.social.dto;

import com.connectsphere.social.model.Reaction;
import com.connectsphere.social.model.ReactionTargetType;
import com.connectsphere.social.model.ReactionType;
import java.time.Instant;

public record ReactionResponse(
        Long reactionId,
        Long userId,
        Long targetId,
        ReactionTargetType targetType,
        ReactionType reactionType,
        Instant createdAt
) {
    public static ReactionResponse from(Reaction reaction) {
        return new ReactionResponse(
                reaction.getReactionId(),
                reaction.getUserId(),
                reaction.getTargetId(),
                reaction.getTargetType(),
                reaction.getReactionType(),
                reaction.getCreatedAt()
        );
    }
}
