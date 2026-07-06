package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.time.Instant;
import java.util.UUID;

public record ProjectDashboardActivityResponse(

        UUID activityId,

        ActivityEntityType entityType,

        UUID entityId,

        ProjectActivityAction action,

        UUID performedByUserId,

        String performedByUsername,

        String displayMessage,

        Instant createdAt

) {
}