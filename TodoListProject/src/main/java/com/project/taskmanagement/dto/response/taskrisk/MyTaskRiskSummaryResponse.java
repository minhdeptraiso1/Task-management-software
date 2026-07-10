package com.project.taskmanagement.dto.response.taskrisk;

import java.util.List;

public record MyTaskRiskSummaryResponse(

        long totalRiskTasks,

        long mediumRiskTasks,

        long highRiskTasks,

        long criticalRiskTasks,

        long overdueTasks,

        long dueSoonTasks,

        long blockedTasks,

        List<TaskRiskResponse> topRisks

) {
}
