package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.service.TaskDueReminderService;
import com.project.taskmanagement.service.model.SchedulerResult;
import com.project.taskmanagement.service.scheduler.SchedulerRunLogger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskDueReminderScheduler {

    static final String JOB_NAME =
            "TASK_DUE_REMINDER";

    TaskDueReminderService taskDueReminderService;
    SchedulerRunLogger schedulerRunLogger;

    @Scheduled(
            cron = "0 0 8 * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    public void runDailyTaskDueReminder() {
        Instant startedAt =
                schedulerRunLogger.start(JOB_NAME);

        try {
            SchedulerResult result =
                    taskDueReminderService.runDailyReminders();

            schedulerRunLogger.success(
                    JOB_NAME,
                    startedAt,
                    result.processedCount(),
                    result.createdCount(),
                    result.skippedCount()
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
