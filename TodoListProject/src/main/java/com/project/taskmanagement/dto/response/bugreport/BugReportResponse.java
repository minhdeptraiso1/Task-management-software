package com.project.taskmanagement.dto.response.bugreport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BugReportResponse(
        UUID projectId,
        UUID sprintId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalBugs,
        long openBugs,
        long resolvedBugs,
        long closedBugs,
        long overdueBugs,
        long criticalBugs,
        long reopenedBugs,
        double resolveRate,
        double overdueRate,
        List<BugCountByStatusResponse> byStatus,
        List<BugCountBySeverityResponse> bySeverity,
        List<BugCountByPriorityResponse> byPriority,
        List<BugAssigneeSummaryResponse> byAssignee
) {
}
