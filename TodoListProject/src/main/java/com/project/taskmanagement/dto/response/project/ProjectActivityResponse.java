package com.project.taskmanagement.dto.response.project;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.time.Instant;
import java.util.UUID;

public record ProjectActivityResponse(

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