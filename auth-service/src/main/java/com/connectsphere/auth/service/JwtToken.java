package com.connectsphere.auth.service;

import java.time.Instant;

public record JwtToken(
        String token,
        Instant expiresAt
) {
}

/*generateToken() ko do cheezein return karni thi —
 *  token string aur expiry time. Java mein ek method se do values return nahi hoti,
 *   toh ek record banao jo dono hold kare. Controller ko expiresAt response mein bhejna hota hai client ko batane ke liye ki token kab expire hoga.
 */