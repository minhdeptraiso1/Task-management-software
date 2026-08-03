package com.project.taskmanagement.security;

import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.service.impl.InMemoryRateLimitServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginRateLimiter redisRateLimiter() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return new LoginRateLimiter(redisTemplate);
    }

    @Test
    void sixthAttemptWithinWindow_isRejected() {
        LoginRateLimiter rateLimiter = redisRateLimiter();
        String key = "login:limited@example.com";
        when(valueOperations.increment(key))
                .thenReturn(1L, 2L, 3L, 4L, 5L, 6L);

        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThatCode(() -> rateLimiter.check(key)).doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> rateLimiter.check(key))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOO_MANY_REQUESTS)
                );
        verify(redisTemplate).expire(key, Duration.ofMinutes(1));
    }

    @Test
    void controllerLoginRateLimitRejectsSixthAttempt() {
        InMemoryRateLimitServiceImpl controllerRateLimiter =
                new InMemoryRateLimitServiceImpl();
        String key = "192.0.2.10:limited@example.com";

        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThatCode(() -> controllerRateLimiter.check(
                    RateLimitAction.LOGIN,
                    key
            )).doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> controllerRateLimiter.check(
                RateLimitAction.LOGIN,
                key
        )).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED)
        );
    }
}
