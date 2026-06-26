package com.project.taskmanagement.dto.response.backlog;

import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;

import java.time.Instant;
import java.util.UUID;

public record BacklogItemResponse(

        UUID id,

        UUID projectId,

        UUID sprintId,

        String title,

        String description,

        BacklogItemType type,

        BacklogItemStatus status,

        BacklogPriority priority,

        Integer storyPoints,

        Long position,

        UUID createdByUserId,

        Instant createdAt,

        Instant updatedAt

) {
}