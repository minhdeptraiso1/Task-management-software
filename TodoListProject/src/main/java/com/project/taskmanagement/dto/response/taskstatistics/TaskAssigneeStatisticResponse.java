package com.project.taskmanagement.dto.response.taskstatistics;

import java.util.UUID;

public record TaskAssigneeStatisticResponse(

        UUID userId,

        String username,

        String email,

        long totalTasks,

        long todoTasks,

        long inProgressTasks,

        long inReviewTasks,

        long doneTasks,

        long blockedTasks,

        long cancelledTasks,

        long estimatedMinutes,

        long spentMinutes,

        double completionRate,

        boolean overEstimated,

        long overEstimatedMinutes

) {
}