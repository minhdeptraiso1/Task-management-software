package com.project.taskmanagement.service.model;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;

import java.util.Collection;
import java.util.UUID;

public record NotificationCommand(
        NotificationType type,
        String title,
        String content,
        UUID actorUserId,
        UUID projectId,
        ActivityEntityType entityType,
        UUID entityId,
        Collection<UUID> recipientUserIds,
        String dedupKey,
        String targetUrl
) {

    public NotificationCommand(
            NotificationType type,
            String title,
            String content,
            UUID actorUserId,
            UUID projectId,
            ActivityEntityType entityType,
            UUID entityId,
            Collection<UUID> recipientUserIds
    ) {
        this(
                type,
                title,
                content,
                actorUserId,
                projectId,
                entityType,
                entityId,
                recipientUserIds,
                null,
                null
        );
    }

    public NotificationCommand(
            NotificationType type,
            String title,
            String content,
            UUID actorUserId,
            UUID projectId,
            ActivityEntityType entityType,
            UUID entityId,
            Collection<UUID> recipientUserIds,
            String dedupKey
    ) {
        this(
                type,
                title,
                content,
                actorUserId,
                projectId,
                entityType,
                entityId,
                recipientUserIds,
                dedupKey,
                null
        );
    }
}
