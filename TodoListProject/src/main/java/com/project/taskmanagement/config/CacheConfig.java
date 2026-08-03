package com.project.taskmanagement.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig implements CachingConfigurer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CacheConfig.class);

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig =
                cacheConfiguration(cacheProperties.getDefaultTtl());

        Map<String, RedisCacheConfiguration> cacheConfigs =
                cacheProperties.getTtl()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> defaultConfig.entryTtl(entry.getValue())
                        ));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    RedisCacheConfiguration cacheConfiguration(Duration ttl) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .computePrefixWith(cacheName ->
                        "task-management:v4:" + cacheName + "::"
                )
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                );
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    RuntimeException exception,
                    Cache cache,
                    Object key
            ) {
                logCacheFailure("get", cache, key, exception);
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception,
                    Cache cache,
                    Object key,
                    Object value
            ) {
                logCacheFailure("put", cache, key, exception);
            }

            @Override
            public void handleCacheEvictError(
                    RuntimeException exception,
                    Cache cache,
                    Object key
            ) {
                logCacheFailure("evict", cache, key, exception);
            }

            @Override
            public void handleCacheClearError(
                    RuntimeException exception,
                    Cache cache
            ) {
                logCacheFailure("clear", cache, null, exception);
            }
        };
    }

    private void logCacheFailure(
            String operation,
            Cache cache,
            Object key,
            RuntimeException exception
    ) {
        LOGGER.warn(
                "Redis cache {} failed; continuing with database. cache={}, key={}, reason={}",
                operation,
                cache == null ? "unknown" : cache.getName(),
                key,
                exception.getMessage()
        );
    }
}
