package com.connectsphere.social.dto;

import java.time.Instant;

public record UserConnectionResponse(
        Long userId,
        String username,
        String fullName,
        String bio,
        String profilePicUrl,
        Instant connectedAt
) {
}
