package com.project.taskmanagement.service.notification;

import com.project.taskmanagement.enums.NotificationType;

import java.time.LocalDate;
import java.util.UUID;

public final class NotificationDedupKeyBuilder {

    private NotificationDedupKeyBuilder() {
    }

    public static String taskDueSoon(
            UUID taskId,
            UUID recipientUserId,
            LocalDate businessDate
    ) {
        return build(
                NotificationType.TASK_DUE_SOON,
                taskId,
                recipientUserId,
                businessDate
        );
    }

    public static String taskOverdue(
            UUID taskId,
            UUID recipientUserId,
            LocalDate businessDate
    ) {
        return build(
                NotificationType.TASK_OVERDUE,
                taskId,
                recipientUserId,
                businessDate
        );
    }

    public static String dailyDigest(
            UUID recipientUserId,
            LocalDate businessDate
    ) {
        return NotificationType.DAILY_DIGEST.name()
                + ":"
                + recipientUserId
                + ":"
                + businessDate;
    }

    private static String build(
            NotificationType type,
            UUID entityId,
            UUID recipientUserId,
            LocalDate businessDate
    ) {
        return type.name()
                + ":"
                + entityId
                + ":"
                + recipientUserId
                + ":"
                + businessDate;
    }
}
