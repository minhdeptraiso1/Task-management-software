package com.project.taskmanagement.dto.response.bugreport;

import java.util.UUID;

public record BugAssigneeSummaryResponse(
        UUID assigneeUserId,
        String username,
        String email,
        long totalBugs,
        long openBugs,
        long resolvedBugs,
        long closedBugs,
        long overdueBugs,
        long criticalBugs,
        long reopenedBugs
) {
}
