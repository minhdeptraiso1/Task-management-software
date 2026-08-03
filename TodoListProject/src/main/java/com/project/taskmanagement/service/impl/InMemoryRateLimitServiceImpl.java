package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.service.model.RateLimitBucket;
import com.project.taskmanagement.service.model.RateLimitRule;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class InMemoryRateLimitServiceImpl
        implements RateLimitService {

    Map<String, RateLimitBucket> buckets =
            new ConcurrentHashMap<>();

    Map<RateLimitAction, RateLimitRule> rules =
            new EnumMap<>(RateLimitAction.class);

    public InMemoryRateLimitServiceImpl() {
        rules.put(
                RateLimitAction.LOGIN,
                new RateLimitRule(
                        5,
                        Duration.ofMinutes(1)
                )
        );

        rules.put(
                RateLimitAction.REFRESH_TOKEN,
                new RateLimitRule(
                        20,
                        Duration.ofMinutes(1)
                )
        );

        rules.put(
                RateLimitAction.TASK_EXCEL_IMPORT,
                new RateLimitRule(
                        5,
                        Duration.ofMinutes(10)
                )
        );

        rules.put(
                RateLimitAction.ATTACHMENT_UPLOAD,
                new RateLimitRule(
                        20,
                        Duration.ofMinutes(10)
                )
        );

        rules.put(
                RateLimitAction.GLOBAL_SEARCH,
                new RateLimitRule(
                        60,
                        Duration.ofMinutes(1)
                )
        );

        rules.put(
                RateLimitAction.PROJECT_SEARCH,
                new RateLimitRule(
                        60,
                        Duration.ofMinutes(1)
                )
        );
    }

    @Override
    public void check(
            RateLimitAction action,
            String key
    ) {
        RateLimitRule rule =
                rules.get(action);

        if (rule == null) {
            return;
        }

        String bucketKey =
                buildBucketKey(
                        action,
                        key
                );

        Instant now =
                Instant.now();

        buckets.compute(
                bucketKey,
                (ignored, bucket) -> {
                    if (bucket == null) {
                        RateLimitBucket newBucket =
                                new RateLimitBucket(now);

                        newBucket.increment();
                        return newBucket;
                    }

                    boolean expired =
                            !bucket
                                    .getWindowStart()
                                    .plus(rule.window())
                                    .isAfter(now);

                    if (expired) {
                        bucket.reset(now);
                    }

                    bucket.increment();

                    if (bucket.getCount()
                            > rule.maxRequests()) {

                        throw new BusinessException(
                                ErrorCode.RATE_LIMIT_EXCEEDED
                        );
                    }

                    return bucket;
                }
        );
    }

    private String buildBucketKey(
            RateLimitAction action,
            String key
    ) {
        return action.name()
                + ":"
                + normalizeKey(key);
    }

    private String normalizeKey(
            String key
    ) {
        if (key == null
                || key.isBlank()) {
            return "anonymous";
        }

        return key.trim().toLowerCase();
    }
}
