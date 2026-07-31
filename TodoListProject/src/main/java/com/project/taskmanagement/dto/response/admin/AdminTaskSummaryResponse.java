package com.project.taskmanagement.dto.response.admin;

public record AdminTaskSummaryResponse(

        long totalTasks,

        long todoTasks,

        long inProgressTasks,

        long inReviewTasks,

        long blockedTasks,

        long doneTasks,

        long cancelledTasks,

        long overdueTasks
) {
}
