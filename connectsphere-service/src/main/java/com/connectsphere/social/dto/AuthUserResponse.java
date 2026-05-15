package com.connectsphere.social.dto;

public record AuthUserResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        String bio,
        String profilePicUrl,
        Object role,
        Object provider,
        boolean active,
        Object createdAt
) {
}
