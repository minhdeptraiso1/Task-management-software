package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.dto.response.taskstatistics.SprintBurndownResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;
import com.project.taskmanagement.enums.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintClosingReportResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        String sprintGoal,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        Instant startedAt,

        Instant completedAt,

        long backlogItemCount,

        long completedBacklogItemCount,

        long unfinishedBacklogItemCount,

        long totalStoryPoints,

        SprintTaskStatisticsResponse taskStatistics,

        SprintBurndownResponse burndown,

        SprintReviewResponse review,

        SprintRetrospectiveResponse retrospective,

        Instant generatedAt

) {
}
