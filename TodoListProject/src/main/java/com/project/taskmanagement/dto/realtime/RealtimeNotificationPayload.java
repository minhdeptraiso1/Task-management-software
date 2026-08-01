package com.project.taskmanagement.dto.realtime;

import com.project.taskmanagement.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record RealtimeNotificationPayload(

        UUID notificationId,

        NotificationType type,

        String title,

        String content,

        UUID projectId,

        String targetUrl,

        UUID actorUserId,

        Instant createdAt,

        long unreadCount
) {
}
