package com.project.taskmanagement.dto.response.taskstatistics;

import java.util.UUID;

public record BacklogItemTaskStatisticResponse(

        UUID backlogItemId,

        String backlogItemTitle,

        long totalTasks,

        long completedTasks,

        long unfinishedTasks,

        long blockedTasks,

        long estimatedMinutes,

        long spentMinutes,

        double completionRate

) {
}