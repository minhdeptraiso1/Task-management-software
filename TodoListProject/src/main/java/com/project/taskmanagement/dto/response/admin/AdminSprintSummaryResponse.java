package com.project.taskmanagement.dto.response.admin;

public record AdminSprintSummaryResponse(

        long totalSprints,

        long planningSprints,

        long activeSprints,

        long completedSprints,

        long cancelledSprints
) {
}
