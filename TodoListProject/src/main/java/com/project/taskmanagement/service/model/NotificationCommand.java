package com.project.taskmanagement.service.model;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;

import java.util.Collection;
import java.util.UUID;

public record NotificationCommand(NotificationType type, String title, String content, UUID actorUserId, UUID projectId,
                                  ActivityEntityType entityType, UUID entityId, Collection<UUID> recipientUserIds) {
}