package com.project.taskmanagement.dto.response.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskDependencyResponse(

        UUID id,

        UUID taskId,

        UUID dependsOnTaskId,

        String dependsOnTaskTitle,

        TaskStatus dependsOnTaskStatus,

        TaskPriority dependsOnTaskPriority,

        UUID dependsOnTaskAssigneeUserId,

        LocalDate dependsOnTaskDueDate,

        boolean dependencyCompleted,

        Instant createdAt

) {
}
