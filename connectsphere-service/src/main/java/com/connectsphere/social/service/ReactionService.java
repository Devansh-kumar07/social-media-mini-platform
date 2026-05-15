package com.connectsphere.social.service;

import com.connectsphere.social.dto.CreateReactionRequest;
import com.connectsphere.social.dto.ReactionResponse;
import com.connectsphere.social.dto.ReactionSummaryResponse;
import com.connectsphere.social.dto.UpdateReactionRequest;
import com.connectsphere.social.model.ReactionTargetType;
import java.util.List;

public interface ReactionService {
    ReactionResponse react(Long userId, CreateReactionRequest request);

    ReactionResponse changeReaction(Long userId, Long targetId, ReactionTargetType targetType, UpdateReactionRequest request);

    void removeReaction(Long userId, Long targetId, ReactionTargetType targetType);

    List<ReactionResponse> getReactionsByUser(Long userId);

    ReactionSummaryResponse getSummary(Long targetId, ReactionTargetType targetType);
}
