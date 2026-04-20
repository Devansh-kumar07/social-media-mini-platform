package com.connectsphere.auth.service;

public record JwtClaims(
        Long userId,
        String email,
        String username,
        String role
) {
}

