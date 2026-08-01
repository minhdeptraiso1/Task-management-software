package com.project.taskmanagement.dto.response.admin;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.time.Instant;
import java.util.UUID;

public record AdminUserActivityResponse(

        UUID id,

        UUID projectId,

        ActivityEntityType entityType,

        UUID entityId,

        ProjectActivityAction action,

        UUID performedByUserId,

        String oldValueJson,

        String newValueJson,

        Instant createdAt
) {
}
