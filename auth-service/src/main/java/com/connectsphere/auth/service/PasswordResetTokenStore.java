package com.connectsphere.auth.service;

import java.time.Duration;
import java.util.Optional;

public interface PasswordResetTokenStore {
    void storeToken(String token, Long userId, Duration ttl);

    Optional<Long> getUserIdByToken(String token);

    void deleteToken(String token);
}
