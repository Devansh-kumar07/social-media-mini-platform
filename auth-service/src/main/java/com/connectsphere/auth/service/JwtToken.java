package com.connectsphere.auth.service;

import java.time.Instant;

public record JwtToken(
        String token,
        Instant expiresAt
) {
}

