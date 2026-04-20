package com.connectsphere.auth.dto;

public record TokenValidationResponse(
        boolean valid,
        Long userId,
        String email,
        String username,
        String role
) {
}

