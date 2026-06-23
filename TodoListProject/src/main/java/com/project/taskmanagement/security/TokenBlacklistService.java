package com.project.taskmanagement.security;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlacklistService {

    RedisTemplate<String, String> redisTemplate;

    public void revoke(String token, Duration ttl) {
        redisTemplate.opsForValue()
                .set(token, "revoked", ttl);
    }

    public boolean isRevoked(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(token)
        );
    }
}
