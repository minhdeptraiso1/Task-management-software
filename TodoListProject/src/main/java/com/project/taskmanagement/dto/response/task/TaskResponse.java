package com.project.taskmanagement.dto.response.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(

        UUID id,

        UUID projectId,

        UUID backlogItemId,

        UUID currentSprintId,

        String title,

        String description,

        TaskType type,

        TaskStatus status,

        TaskPriority priority,

        UUID assigneeUserId,

        String assigneeUsername,

        String assigneeEmail,

        UUID reporterUserId,

        Integer estimatedMinutes,

        Long spentMinutes,

        LocalDate startDate,

        LocalDate dueDate,

        Instant completedAt,

        Long position,

        Instant createdAt,

        Instant updatedAt

) {
}