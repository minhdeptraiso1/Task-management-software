package com.project.taskmanagement.dto.response.task;

import com.project.taskmanagement.enums.TaskRiskLevel;
import com.project.taskmanagement.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskRiskResponse(

        UUID taskId,

        UUID projectId,

        String title,

        TaskStatus status,

        LocalDate dueDate,

        TaskRiskLevel riskLevel,

        boolean overdue,

        boolean blocked,

        boolean hasUnfinishedDependencies,

        long unfinishedDependencyCount,

        List<String> reasons,

        String targetUrl

) {
}
