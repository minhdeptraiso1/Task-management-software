package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskSearchRequest(

        String keyword,

        UUID backlogItemId,

        UUID sprintId,

        UUID assigneeUserId,

        UUID reporterUserId,

        TaskStatus status,

        TaskPriority priority,

        TaskType type,

        Boolean unassignedOnly,

        Boolean overdueOnly,

        Boolean dueSoonOnly,

        LocalDate startDateFrom,

        LocalDate startDateTo,

        LocalDate dueDateFrom,

        LocalDate dueDateTo,

        Instant createdFrom,

        Instant createdTo

) {
}
