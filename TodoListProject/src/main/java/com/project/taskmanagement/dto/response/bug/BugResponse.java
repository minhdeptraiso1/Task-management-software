package com.project.taskmanagement.dto.response.bug;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BugResponse(
        UUID id,
        UUID projectId,
        UUID backlogItemId,
        UUID taskId,
        UUID sprintId,
        String title,
        String description,
        BugSeverity severity,
        TaskPriority priority,
        BugStatus status,
        UUID assigneeUserId,
        String assigneeUsername,
        String assigneeEmail,
        UUID reporterUserId,
        String reporterUsername,
        String reporterEmail,
        String reproductionSteps,
        String expectedResult,
        String actualResult,
        LocalDate dueDate,
        Integer reopenedCount,
        boolean overdue,
        Instant resolvedAt,
        Instant closedAt,
        String targetUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
