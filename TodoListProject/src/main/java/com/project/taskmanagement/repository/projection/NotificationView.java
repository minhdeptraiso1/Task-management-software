package com.project.taskmanagement.repository.projection;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public interface NotificationView {

    UUID getId();

    NotificationType getType();

    String getTitle();

    String getContent();

    UUID getActorUserId();

    UUID getProjectId();

    ActivityEntityType getEntityType();

    UUID getEntityId();

    String getTargetUrl();

    Instant getCreatedAt();

    Instant getDeliveredAt();

    Instant getReadAt();
}
