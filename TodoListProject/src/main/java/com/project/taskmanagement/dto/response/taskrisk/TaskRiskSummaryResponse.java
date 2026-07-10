package com.project.taskmanagement.dto.response.taskrisk;

import java.util.List;
import java.util.UUID;

public record TaskRiskSummaryResponse(

        UUID projectId,

        UUID sprintId,

        long totalRiskTasks,

        long lowRiskTasks,

        long mediumRiskTasks,

        long highRiskTasks,

        long criticalRiskTasks,

        long overdueTasks,

        long dueSoonTasks,

        long blockedTasks,

        long dependencyRiskTasks,

        List<TaskRiskResponse> topRisks

) {
}
