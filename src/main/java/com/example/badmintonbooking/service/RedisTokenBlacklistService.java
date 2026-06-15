package com.example.badmintonbooking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public void addToBlacklist(String token, long expirationTimeInMillis) {
        long remainingTime = expirationTimeInMillis - System.currentTimeMillis();
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + token, 
                    "revoked", 
                    Duration.ofMillis(remainingTime)
            );
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
