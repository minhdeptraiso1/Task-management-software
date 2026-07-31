package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.scheduler.DailyDigestRunResponse;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.DailyDigestService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.model.DailyDigestData;
import com.project.taskmanagement.service.model.NotificationCommand;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class DailyDigestServiceImpl
        implements DailyDigestService {

    UserRepository userRepository;
    TaskRepository taskRepository;
    NotificationRepository notificationRepository;
    NotificationRecipientRepository notificationRecipientRepository;
    NotificationService notificationService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DUE_SOON_DAYS = 3;

    static final List<TaskStatus> CLOSED_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    @Override
    @Transactional
    public DailyDigestRunResponse runDailyDigest(
            LocalDate businessDate
    ) {
        LocalDate resolvedDate =
                businessDate == null
                        ? LocalDate.now(BUSINESS_ZONE)
                        : businessDate;

        List<User> users =
                userRepository.findAllByEnabledTrue();

        long sentDigests = 0L;
        long skippedUsers = 0L;

        for (User user : users) {
            DailyDigestData digestData =
                    buildDigestData(
                            user,
                            resolvedDate
                    );

            if (!digestData.hasContent()) {
                skippedUsers++;
                continue;
            }

            String dedupKey =
                    NotificationDedupKeyBuilder.dailyDigest(
                            user.getId(),
                            resolvedDate
                    );

            if (notificationRepository.existsByTypeAndDedupKey(
                    NotificationType.DAILY_DIGEST,
                    dedupKey
            )) {
                skippedUsers++;
                continue;
            }

            sendDigest(
                    user,
                    digestData,
                    resolvedDate,
                    dedupKey
            );

            sentDigests++;
        }

        return new DailyDigestRunResponse(
                resolvedDate,
                users.size(),
                sentDigests,
                skippedUsers
        );
    }

    private DailyDigestData buildDigestData(
            User user,
            LocalDate businessDate
    ) {
        LocalDate dueSoonToDate =
                businessDate.plusDays(DUE_SOON_DAYS);

        List<Task> tasks =
                taskRepository.findTasksForDailyDigest(
                        user.getId(),
                        CLOSED_STATUSES,
                        dueSoonToDate
                );

        List<Task> overdueTasks =
                new ArrayList<>();

        List<Task> dueTodayTasks =
                new ArrayList<>();

        List<Task> dueSoonTasks =
                new ArrayList<>();

        List<Task> blockedTasks =
                new ArrayList<>();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.BLOCKED) {
                blockedTasks.add(task);
            }

            if (task.getDueDate() == null) {
                continue;
            }

            if (task.getDueDate().isBefore(businessDate)) {
                overdueTasks.add(task);
                continue;
            }

            if (task.getDueDate().isEqual(businessDate)) {
                dueTodayTasks.add(task);
                continue;
            }

            if (!task.getDueDate().isAfter(dueSoonToDate)) {
                dueSoonTasks.add(task);
            }
        }

        long unreadNotificationCount =
                notificationRecipientRepository.countUnreadByUserId(
                        user.getId()
                );

        return new DailyDigestData(
                user.getId(),
                overdueTasks,
                dueTodayTasks,
                dueSoonTasks,
                blockedTasks,
                unreadNotificationCount
        );
    }

    private void sendDigest(
            User user,
            DailyDigestData data,
            LocalDate businessDate,
            String dedupKey
    ) {
        notificationService.create(
                new NotificationCommand(
                        NotificationType.DAILY_DIGEST,
                        "Daily Digest - " + businessDate,
                        buildContent(data),
                        null,
                        null,
                        null,
                        null,
                        List.of(user.getId()),
                        dedupKey,
                        "/dashboard/me"
                )
        );
    }

    private String buildContent(
            DailyDigestData data
    ) {
        StringBuilder builder =
                new StringBuilder();

        appendLine(
                builder,
                "Task quá hạn",
                data.overdueTasks().size()
        );

        appendLine(
                builder,
                "Task đến hạn hôm nay",
                data.dueTodayTasks().size()
        );

        appendLine(
                builder,
                "Task sắp đến hạn",
                data.dueSoonTasks().size()
        );

        appendLine(
                builder,
                "Task đang bị block",
                data.blockedTasks().size()
        );

        appendLine(
                builder,
                "Notification chưa đọc",
                data.unreadNotificationCount()
        );

        return builder.toString();
    }

    private void appendLine(
            StringBuilder builder,
            String label,
            long value
    ) {
        if (value <= 0) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append("\n");
        }

        builder.append(label)
                .append(": ")
                .append(value);
    }

}
