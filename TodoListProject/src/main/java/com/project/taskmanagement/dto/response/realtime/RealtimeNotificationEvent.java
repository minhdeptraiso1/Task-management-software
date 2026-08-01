package com.project.taskmanagement.dto.response.realtime;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record RealtimeNotificationEvent(

        UUID notificationId,

        NotificationType type,

        String title,

        String content,

        UUID actorUserId,

        UUID projectId,

        ActivityEntityType entityType,

        UUID entityId,

        String targetUrl,

        Instant createdAt
) {
}
