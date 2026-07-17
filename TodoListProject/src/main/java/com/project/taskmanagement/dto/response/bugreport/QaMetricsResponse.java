package com.project.taskmanagement.dto.response.bugreport;

import java.time.LocalDate;
import java.util.UUID;

public record QaMetricsResponse(
        UUID projectId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalBugsReported,
        long totalBugsResolved,
        long totalBugsClosed,
        long totalBugsReopened,
        long totalCriticalBugs,
        long totalOverdueBugs,
        double resolveRate,
        double reopenRate,
        double overdueRate,
        double criticalRate
) {
}
