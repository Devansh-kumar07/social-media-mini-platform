package com.connectsphere.social.service;

import com.connectsphere.social.dto.FollowResponse;
import com.connectsphere.social.dto.UserConnectionResponse;
import java.util.List;

public interface FollowService {
    FollowResponse follow(Long followerId, Long followeeId);

    void unfollow(Long followerId, Long followeeId);

    boolean isFollowing(Long followerId, Long followeeId);

    List<UserConnectionResponse> getFollowers(Long userId);

    List<UserConnectionResponse> getFollowing(Long userId);

    long getFollowerCount(Long userId);

    long getFollowingCount(Long userId);

    List<Long> getMutualConnections(Long userId, Long otherUserId);

    List<Long> getSuggestedUsers(Long userId);
}
