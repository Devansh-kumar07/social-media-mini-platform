package com.connectsphere.social.controller;

import com.connectsphere.social.dto.FollowResponse;
import com.connectsphere.social.dto.UserConnectionResponse;
import com.connectsphere.social.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{followeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Follow a user", security = @SecurityRequirement(name = "x-user-id"))
    public FollowResponse follow(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("followeeId") Long followeeId
    ) {
        return followService.follow(userId, followeeId);
    }

    @DeleteMapping("/{followeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unfollow a user", security = @SecurityRequirement(name = "x-user-id"))
    public void unfollow(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable("followeeId") Long followeeId
    ) {
        followService.unfollow(userId, followeeId);
    }

    @GetMapping("/status")
    @Operation(summary = "Check if user follows another user")
    public boolean isFollowing(
            @RequestParam(name = "followerId") Long followerId,
            @RequestParam(name = "followeeId") Long followeeId
    ) {
        return followService.isFollowing(followerId, followeeId);
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get followers")
    public List<UserConnectionResponse> getFollowers(@PathVariable("userId") Long userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get following")
    public List<UserConnectionResponse> getFollowing(@PathVariable("userId") Long userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/followers/{userId}/count")
    @Operation(summary = "Get follower count")
    public long getFollowerCount(@PathVariable("userId") Long userId) {
        return followService.getFollowerCount(userId);
    }

    @GetMapping("/following/{userId}/count")
    @Operation(summary = "Get following count")
    public long getFollowingCount(@PathVariable("userId") Long userId) {
        return followService.getFollowingCount(userId);
    }

    @GetMapping("/mutual")
    @Operation(summary = "Get mutual connections")
    public List<Long> getMutualConnections(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "otherUserId") Long otherUserId
    ) {
        return followService.getMutualConnections(userId, otherUserId);
    }

    @GetMapping("/suggestions/{userId}")
    @Operation(summary = "Get suggested users to follow")
    public List<Long> getSuggestions(@PathVariable("userId") Long userId) {
        return followService.getSuggestedUsers(userId);
    }
}
