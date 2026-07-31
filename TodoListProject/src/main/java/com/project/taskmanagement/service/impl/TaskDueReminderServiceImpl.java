package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.TaskDueReminderService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.SchedulerResult;
import com.project.taskmanagement.service.notification.NotificationDedupKeyBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskDueReminderServiceImpl
        implements TaskDueReminderService {

    TaskRepository taskRepository;
    NotificationRepository notificationRepository;
    NotificationService notificationService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DUE_SOON_DAYS = 3;

    static final List<TaskStatus> EXCLUDED_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    @Override
    @Transactional
    public SchedulerResult runDailyReminders() {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        SchedulerResult dueSoonResult =
                sendDueSoonReminders(today);

        SchedulerResult overdueResult =
                sendOverdueReminders(today);

        return new SchedulerResult(
                dueSoonResult.processedCount()
                        + overdueResult.processedCount(),
                dueSoonResult.createdCount()
                        + overdueResult.createdCount(),
                dueSoonResult.skippedCount()
                        + overdueResult.skippedCount()
        );
    }

    @Override
    @Transactional
    public void sendDueSoonReminders() {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        sendDueSoonReminders(today);
    }

    @Override
    @Transactional
    public void sendOverdueReminders() {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        sendOverdueReminders(today);
    }

    private SchedulerResult sendDueSoonReminders(
            LocalDate today
    ) {

        LocalDate toDate =
                today.plusDays(DUE_SOON_DAYS);

        List<Task> tasks =
                taskRepository.findDueSoonTasks(
                        today,
                        toDate,
                        EXCLUDED_STATUSES
                );

        long created = 0L;
        long skipped = 0L;

        for (Task task : tasks) {
            if (sendDueSoonReminder(
                    task,
                    today
            )) {
                created++;
            } else {
                skipped++;
            }
        }

        return new SchedulerResult(
                tasks.size(),
                created,
                skipped
        );
    }

    private SchedulerResult sendOverdueReminders(
            LocalDate today
    ) {
        List<Task> tasks =
                taskRepository.findOverdueTasks(
                        today,
                        EXCLUDED_STATUSES
                );

        long processed = 0L;
        long created = 0L;
        long skipped = 0L;

        for (Task task : tasks) {
            SchedulerResult result =
                    sendOverdueReminder(
                    task,
                    today
            );

            processed += result.processedCount();
            created += result.createdCount();
            skipped += result.skippedCount();
        }

        return new SchedulerResult(
                processed,
                created,
                skipped
        );
    }

    private boolean sendDueSoonReminder(
            Task task,
            LocalDate businessDate
    ) {
        if (task.getAssigneeUserId() == null) {
            return false;
        }

        UUID recipientId =
                task.getAssigneeUserId();

        String dedupKey =
                NotificationDedupKeyBuilder.taskDueSoon(
                        task.getId(),
                        recipientId,
                        businessDate
                );

        if (notificationRepository.existsByDedupKey(dedupKey)) {
            return false;
        }

        notificationService.create(
                new NotificationCommand(
                        NotificationType.TASK_DUE_SOON,
                        "Task sắp đến hạn",
                        "Task \"" + task.getTitle() + "\" sắp đến hạn vào "
                                + task.getDueDate(),
                        null,
                        task.getProjectId(),
                        ActivityEntityType.TASK,
                        task.getId(),
                        List.of(recipientId),
                        dedupKey
                )
        );

        return true;
    }

    private SchedulerResult sendOverdueReminder(
            Task task,
            LocalDate businessDate
    ) {
        List<UUID> recipientIds =
                buildOverdueRecipientIds(task);

        long created = 0L;
        long skipped = 0L;

        for (UUID recipientId : recipientIds) {
            String dedupKey =
                    NotificationDedupKeyBuilder.taskOverdue(
                            task.getId(),
                            recipientId,
                            businessDate
                    );

            if (notificationRepository.existsByDedupKey(dedupKey)) {
                skipped++;
                continue;
            }

            notificationService.create(
                    new NotificationCommand(
                            NotificationType.TASK_OVERDUE,
                            "Task quá hạn",
                            "Task \"" + task.getTitle() + "\" đã quá hạn từ "
                                    + task.getDueDate(),
                            null,
                            task.getProjectId(),
                            ActivityEntityType.TASK,
                            task.getId(),
                            List.of(recipientId),
                            dedupKey
                    )
            );

            created++;
        }

        return new SchedulerResult(
                recipientIds.size(),
                created,
                skipped
        );
    }

    private List<UUID> buildOverdueRecipientIds(
            Task task
    ) {
        List<UUID> recipientIds =
                new ArrayList<>();

        if (task.getAssigneeUserId() != null) {
            recipientIds.add(
                    task.getAssigneeUserId()
            );
        }

        if (task.getReporterUserId() != null
                && !recipientIds.contains(
                task.getReporterUserId()
        )) {
            recipientIds.add(
                    task.getReporterUserId()
            );
        }

        return recipientIds;
    }

}
