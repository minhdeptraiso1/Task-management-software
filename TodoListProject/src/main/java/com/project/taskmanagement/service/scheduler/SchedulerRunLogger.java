package com.project.taskmanagement.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class SchedulerRunLogger {

    public Instant start(
            String jobName
    ) {
        Instant startedAt =
                Instant.now();

        log.info(
                "[SCHEDULER-START] job={}, startedAt={}",
                jobName,
                startedAt
        );

        return startedAt;
    }

    public void success(
            String jobName,
            Instant startedAt,
            long processedCount,
            long createdCount,
            long skippedCount
    ) {
        log.info(
                "[SCHEDULER-SUCCESS] job={}, durationMs={}, processed={}, created={}, skipped={}",
                jobName,
                Duration.between(
                        startedAt,
                        Instant.now()
                ).toMillis(),
                processedCount,
                createdCount,
                skippedCount
        );
    }

    public void failed(
            String jobName,
            Instant startedAt,
            Exception exception
    ) {
        log.error(
                "[SCHEDULER-FAILED] job={}, durationMs={}, error={}",
                jobName,
                Duration.between(
                        startedAt,
                        Instant.now()
                ).toMillis(),
                exception.getMessage(),
                exception
        );
    }
}
