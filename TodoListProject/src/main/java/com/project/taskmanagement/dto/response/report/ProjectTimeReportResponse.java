package com.project.taskmanagement.dto.response.report;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectTimeReportResponse(

        UUID projectId,

        String projectCode,

        String projectName,

        LocalDate fromDate,

        LocalDate toDate,

        UUID filteredUserId,

        UUID filteredTaskId,

        long totalMinutes,

        long totalLogs,

        long taskCount,

        long contributorCount,

        long totalEstimatedMinutes,

        long remainingEstimatedMinutes,

        double timeUsageRate,

        boolean overEstimated,

        long overEstimatedMinutes,

        List<ProjectTimeDailyResponse> byDate,

        List<ProjectTimeMemberResponse> byMember,

        List<ProjectTimeTaskResponse> byTask,

        Instant generatedAt

) {
}