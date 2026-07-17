package com.project.taskmanagement.dto.response.bugreport;

import java.util.List;
import java.util.UUID;

public record BugDashboardResponse(
        UUID projectId,
        String projectCode,
        String projectName,
        long totalBugs,
        long openBugs,
        long inProgressBugs,
        long resolvedBugs,
        long closedBugs,
        long cancelledBugs,
        long overdueBugs,
        long criticalBugs,
        long reopenedBugs,
        double resolveRate,
        List<BugCountByStatusResponse> byStatus,
        List<BugCountBySeverityResponse> bySeverity,
        List<BugCountByPriorityResponse> byPriority,
        List<BugAssigneeSummaryResponse> byAssignee
) {
}
