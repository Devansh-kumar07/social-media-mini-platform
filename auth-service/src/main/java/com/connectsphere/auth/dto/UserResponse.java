package com.connectsphere.auth.dto;

import com.connectsphere.auth.model.AuthProvider;
import com.connectsphere.auth.model.User;
import com.connectsphere.auth.model.UserRole;
import java.time.Instant;

public record UserResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        String bio,
        String profilePicUrl,
        UserRole role,
        AuthProvider provider,
        boolean active,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getProfilePicUrl(),
                user.getRole(),
                user.getProvider(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}

