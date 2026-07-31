package com.project.taskmanagement.dto.response.admin;

public record AdminBugSummaryResponse(

        long totalBugs,

        long openBugs,

        long inProgressBugs,

        long resolvedBugs,

        long closedBugs,

        long criticalBugs
) {
}
