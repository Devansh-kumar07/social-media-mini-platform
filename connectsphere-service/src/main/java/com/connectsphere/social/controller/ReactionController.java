package com.connectsphere.social.controller;

import com.connectsphere.social.dto.CreateReactionRequest;
import com.connectsphere.social.dto.ReactionResponse;
import com.connectsphere.social.dto.ReactionSummaryResponse;
import com.connectsphere.social.dto.UpdateReactionRequest;
import com.connectsphere.social.model.ReactionTargetType;
import com.connectsphere.social.service.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {
    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "React to a post or comment", security = @SecurityRequirement(name = "x-user-id"))
    public ReactionResponse react(
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody CreateReactionRequest request
    ) {
        return reactionService.react(userId, request);
    }

    @PutMapping("/{targetType}/{targetId}")
    @Operation(summary = "Change a reaction", security = @SecurityRequirement(name = "x-user-id"))
    public ReactionResponse changeReaction(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("targetType") ReactionTargetType targetType,
            @PathVariable("targetId") Long targetId,
            @Valid @RequestBody UpdateReactionRequest request
    ) {
        return reactionService.changeReaction(userId, targetId, targetType, request);
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a reaction", security = @SecurityRequirement(name = "x-user-id"))
    public void removeReaction(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("targetType") ReactionTargetType targetType,
            @PathVariable("targetId") Long targetId
    ) {
        reactionService.removeReaction(userId, targetId, targetType);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reactions by user")
    public List<ReactionResponse> getReactionsByUser(@PathVariable("userId") Long userId) {
        return reactionService.getReactionsByUser(userId);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get reaction summary by target")
    public ReactionSummaryResponse getSummary(
            @RequestParam(name = "targetId") Long targetId,
            @RequestParam(name = "targetType") ReactionTargetType targetType
    ) {
        return reactionService.getSummary(targetId, targetType);
    }
}
