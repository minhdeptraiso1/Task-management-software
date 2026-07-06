package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record MyUpcomingTaskResponse(

        UUID taskId,

        UUID projectId,

        String projectCode,

        String projectName,

        UUID backlogItemId,

        UUID currentSprintId,

        String title,

        TaskStatus status,

        TaskPriority priority,

        LocalDate startDate,

        LocalDate dueDate,

        boolean overdue,

        boolean dueSoon,

        long daysUntilDue,

        String targetUrl

) {
}