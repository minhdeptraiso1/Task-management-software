package com.project.taskmanagement.dto.response.taskrisk;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskRiskLevel;
import com.project.taskmanagement.enums.TaskRiskReason;
import com.project.taskmanagement.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskRiskResponse(

        UUID taskId,

        UUID projectId,

        UUID sprintId,

        String title,

        TaskStatus status,

        TaskPriority priority,

        UUID assigneeUserId,

        String assigneeUsername,

        String assigneeEmail,

        LocalDate dueDate,

        boolean overdue,

        boolean dueSoon,

        boolean blocked,

        long daysUntilDue,

        long blockingTaskCount,

        long unresolvedDependencyCount,

        TaskRiskLevel riskLevel,

        List<TaskRiskReason> reasons,

        String targetUrl

) {
}
