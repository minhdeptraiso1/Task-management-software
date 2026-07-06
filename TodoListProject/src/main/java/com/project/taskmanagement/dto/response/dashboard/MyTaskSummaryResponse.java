package com.project.taskmanagement.dto.response.dashboard;

public record MyTaskSummaryResponse(

        long totalTasks,

        long todoTasks,

        long inProgressTasks,

        long inReviewTasks,

        long blockedTasks,

        long doneTasks,

        long cancelledTasks,

        long overdueTasks,

        long dueSoonTasks

) {
}