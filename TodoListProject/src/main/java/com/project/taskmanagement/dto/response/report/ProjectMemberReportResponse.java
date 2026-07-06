package com.project.taskmanagement.dto.response.report;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectMemberReportResponse(

        UUID projectId,

        String projectCode,

        String projectName,

        LocalDate fromDate,

        LocalDate toDate,

        long totalMembers,

        long membersWithTasks,

        long membersWithTimeLogs,

        long totalAssignedTasks,

        long totalEstimatedMinutes,

        long totalSpentMinutes,

        List<ProjectMemberReportItemResponse> members,

        Instant generatedAt

) {
}