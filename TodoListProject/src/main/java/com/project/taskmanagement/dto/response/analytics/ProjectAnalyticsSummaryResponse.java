package com.project.taskmanagement.dto.response.analytics;

import java.util.UUID;

public record ProjectAnalyticsSummaryResponse(

        UUID projectId,

        String projectCode,

        String projectName,

        long totalSprints,

        long completedSprints,

        long activeSprints,

        long totalBacklogItems,

        long completedBacklogItems,

        long totalTasks,

        long completedTasks,

        long blockedTasks,

        long overdueTasks,

        long estimatedMinutes,

        long spentMinutes,

        double taskCompletionRate,

        double backlogCompletionRate
) {
}
