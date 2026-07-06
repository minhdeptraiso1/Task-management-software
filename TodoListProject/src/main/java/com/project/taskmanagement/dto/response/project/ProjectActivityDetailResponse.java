package com.project.taskmanagement.dto.response.project;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.time.Instant;
import java.util.UUID;

public record ProjectActivityDetailResponse(

        UUID id,

        UUID projectId,

        ActivityEntityType entityType,

        UUID entityId,

        ProjectActivityAction action,

        UUID performedByUserId,

        String performedByUsername,

        String performedByEmail,

        String oldValueJson,

        String newValueJson,

        String displayMessage,

        Instant createdAt

) {
}