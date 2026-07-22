package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.service.FileCleanupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCleanupScheduler {

    FileCleanupService fileCleanupService;

    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Ho_Chi_Minh")
    public void cleanupDeletedFiles() {
        fileCleanupService.cleanupDeletedAttachmentFiles(500);
    }
}
