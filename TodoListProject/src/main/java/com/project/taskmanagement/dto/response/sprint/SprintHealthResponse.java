package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.SprintHealthStatus;
import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintHealthResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        SprintHealthStatus healthStatus,

        LocalDate startDate,

        LocalDate endDate,

        long totalDays,

        long elapsedDays,

        long remainingDays,

        long totalTasks,

        long completedTasks,

        long unfinishedTasks,

        long blockedTasks,

        long overdueTasks,

        long noAssigneeTasks,

        long noEstimateTasks,

        double progressRate,

        double expectedProgressRate,

        double timeUsageRate,

        boolean overCapacity,

        long totalCapacityMinutes,

        long totalEstimatedMinutes,

        long totalSpentMinutes,

        long riskCount,

        long criticalRiskCount,

        List<SprintRiskResponse> topRisks

) {
}
