package com.connectsphere.auth.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPasswordResetTokenStore implements PasswordResetTokenStore {
    private static final String KEY_PREFIX = "auth:password-reset:";

    private final StringRedisTemplate redisTemplate;

    public RedisPasswordResetTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeToken(String token, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + token, String.valueOf(userId), ttl);
    }

    @Override
    public Optional<Long> getUserIdByToken(String token) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(value));
    }

    @Override
    public void deleteToken(String token) {
        redisTemplate.delete(KEY_PREFIX + token);
    }
}
