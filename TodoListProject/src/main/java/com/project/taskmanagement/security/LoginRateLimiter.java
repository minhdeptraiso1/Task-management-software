package com.project.taskmanagement.security;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginRateLimiter {

    RedisTemplate<String, String> redisTemplate;

    static final int MAX_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(1);

    public void check(String key) {

        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        if (attempts > MAX_ATTEMPTS) {
            // ⭐ THROW BUSINESS EXCEPTION CHUẨN
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    public void reset(String key) {
        redisTemplate.delete(key);
    }
}
