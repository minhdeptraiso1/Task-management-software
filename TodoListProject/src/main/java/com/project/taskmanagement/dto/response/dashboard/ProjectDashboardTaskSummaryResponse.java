package com.project.taskmanagement.dto.response.dashboard;

import java.util.List;

public record ProjectDashboardTaskSummaryResponse(

        long totalTasks,

        long completedTasks,

        long unfinishedTasks,

        long blockedTasks,

        long cancelledTasks,

        long overdueTasks,

        double completionRate,

        long estimatedMinutes,

        long spentMinutes,

        long remainingEstimatedMinutes,

        double timeUsageRate,

        boolean overEstimated,

        long overEstimatedMinutes,

        List<ProjectDashboardTaskStatusResponse> byStatus

) {
}