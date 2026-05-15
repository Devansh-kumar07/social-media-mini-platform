package com.connectsphere.social.dto;

import com.connectsphere.social.model.Follow;
import java.time.Instant;

public record FollowResponse(
        Long followId,
        Long followerId,
        Long followeeId,
        Instant createdAt
) {
    public static FollowResponse from(Follow follow) {
        return new FollowResponse(
                follow.getFollowId(),
                follow.getFollowerId(),
                follow.getFolloweeId(),
                follow.getCreatedAt()
        );
    }
}
