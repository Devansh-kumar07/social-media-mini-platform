package com.connectsphere.social.service;

import com.connectsphere.social.client.AuthServiceClient;
import com.connectsphere.social.client.NotificationServiceClient;
import com.connectsphere.social.dto.AuthUserResponse;
import com.connectsphere.social.dto.FollowResponse;
import com.connectsphere.social.dto.UserConnectionResponse;
import com.connectsphere.social.model.Follow;
import com.connectsphere.social.repository.FollowRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public FollowServiceImpl(
            FollowRepository followRepository,
            AuthServiceClient authServiceClient,
            NotificationServiceClient notificationServiceClient
    ) {
        this.followRepository = followRepository;
        this.authServiceClient = authServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    @Transactional
    public FollowResponse follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new IllegalArgumentException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        FollowResponse response = FollowResponse.from(followRepository.save(follow));

        notificationServiceClient.sendNotification(
                followeeId,
                followerId,
                "FOLLOW",
                "Someone started following you"
        );

        return response;
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new IllegalArgumentException("Follow relationship not found"));
        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserConnectionResponse> getFollowers(Long userId) {
        return followRepository.findByFolloweeIdOrderByCreatedAtDesc(userId).stream()
                .map(follow -> toUserConnectionResponse(follow.getFollowerId(), follow.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserConnectionResponse> getFollowing(Long userId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId).stream()
                .map(follow -> toUserConnectionResponse(follow.getFolloweeId(), follow.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMutualConnections(Long userId, Long otherUserId) {
        Set<Long> firstUserFollowing = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId).stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toSet());
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(otherUserId).stream()
                .map(Follow::getFolloweeId)
                .filter(firstUserFollowing::contains)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getSuggestedUsers(Long userId) {
        Set<Long> directFollowing = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId).stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toSet());
        Set<Long> suggestions = new LinkedHashSet<>();

        for (Long followeeId : directFollowing) {
            followRepository.findByFollowerIdOrderByCreatedAtDesc(followeeId).stream()
                    .map(Follow::getFolloweeId)
                    .filter(candidate -> !candidate.equals(userId))
                    .filter(candidate -> !directFollowing.contains(candidate))
                    .forEach(suggestions::add);
        }

        return suggestions.stream().limit(10).toList();
    }

    private UserConnectionResponse toUserConnectionResponse(Long userId, Instant connectedAt) {
        AuthUserResponse user = authServiceClient.getUserById(userId);
        if (user == null) {
            return new UserConnectionResponse(userId, null, null, null, null, connectedAt);
        }

        return new UserConnectionResponse(
                user.userId(),
                user.username(),
                user.fullName(),
                user.bio(),
                user.profilePicUrl(),
                connectedAt
        );
    }
}
