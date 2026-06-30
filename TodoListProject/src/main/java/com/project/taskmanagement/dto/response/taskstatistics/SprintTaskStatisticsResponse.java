package com.project.taskmanagement.dto.response.taskstatistics;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintTaskStatisticsResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

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

        List<TaskStatusStatisticResponse> byStatus,

        List<TaskAssigneeStatisticResponse> byAssignee,

        List<BacklogItemTaskStatisticResponse> byBacklogItem

) {
}