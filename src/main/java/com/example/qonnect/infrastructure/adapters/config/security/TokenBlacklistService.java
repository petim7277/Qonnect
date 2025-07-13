package com.example.qonnect.infrastructure.adapters.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String jti, long expirationMillis) {
        String key = "blacklist:" + jti;
        try {
            redisTemplate.opsForValue().set(key, "blacklisted", expirationMillis, TimeUnit.SECONDS);
            log.info("Redis key saved successfully");
        } catch (Exception e) {
            log.error("Failed to save jti in Redis: {}", e.getMessage(), e);
        }
    }

    public boolean isBlacklisted(String jti) {
        String key = "blacklist:" + jti;
        log.info("🔍 Checking Redis for key: {}", key);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
