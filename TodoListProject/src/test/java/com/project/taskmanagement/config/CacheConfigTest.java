package com.project.taskmanagement.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheConfigTest {

    @Test
    void appliesPerCacheTtlAndSerializesJavaTimeRecord() {
        CacheProperties properties = new CacheProperties();
        CacheConfig cacheConfig = new CacheConfig(properties);
        RedisCacheConfiguration configuration =
                cacheConfig.cacheConfiguration(Duration.ofMinutes(30));

        assertEquals(Duration.ofMinutes(30), configuration.getTtl());

        CacheSample expected = new CacheSample(
                LocalDate.of(2026, 8, 3),
                Instant.parse("2026-08-03T01:00:00Z")
        );
        ByteBuffer bytes = configuration
                .getValueSerializationPair()
                .write(expected);
        Object actual = configuration
                .getValueSerializationPair()
                .read(bytes);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    private record CacheSample(
            LocalDate date,
            Instant createdAt
    ) {
    }
}
