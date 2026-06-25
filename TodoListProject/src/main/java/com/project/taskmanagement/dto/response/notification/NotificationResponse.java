package com.project.taskmanagement.dto.response.notification;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        NotificationType type,

        String title,

        String content,

        UUID actorUserId,

        UUID projectId,

        ActivityEntityType entityType,

        UUID entityId,

        Instant createdAt,

        Instant deliveredAt,

        Instant readAt,

        boolean read

) {
}