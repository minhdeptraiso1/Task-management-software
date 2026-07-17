package com.project.taskmanagement.dto.response.bug;

import java.util.Map;
import java.util.UUID;

public record BugSummaryResponse(
        UUID projectId,
        long totalBugs,
        long openBugs,
        long assignedStatusBugs,
        long inProgressBugs,
        long resolvedBugs,
        long verifiedBugs,
        long reopenedBugs,
        long closedBugs,
        long cancelledBugs,
        long criticalBugs,
        long highBugs,
        long assignedBugs,
        long unassignedBugs,
        Map<String, Long> byStatus,
        Map<String, Long> bySeverity
) {
}
