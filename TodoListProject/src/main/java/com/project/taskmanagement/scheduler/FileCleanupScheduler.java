package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.service.FileCleanupService;
import com.project.taskmanagement.service.scheduler.SchedulerRunLogger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCleanupScheduler {

    static final String JOB_NAME =
            "FILE_CLEANUP";

    static final int BATCH_SIZE = 500;

    FileCleanupService fileCleanupService;
    SchedulerRunLogger schedulerRunLogger;

    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Ho_Chi_Minh")
    public void cleanupDeletedFiles() {
        Instant startedAt =
                schedulerRunLogger.start(JOB_NAME);

        try {
            fileCleanupService.cleanupDeletedAttachmentFiles(BATCH_SIZE);

            schedulerRunLogger.success(
                    JOB_NAME,
                    startedAt,
                    BATCH_SIZE,
                    0,
                    0
            );
        } catch (Exception exception) {
            schedulerRunLogger.failed(
                    JOB_NAME,
                    startedAt,
                    exception
            );
        }
    }
}
