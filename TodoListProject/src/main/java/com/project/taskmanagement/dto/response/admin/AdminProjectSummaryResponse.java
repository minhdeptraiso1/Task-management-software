package com.project.taskmanagement.dto.response.admin;

public record AdminProjectSummaryResponse(

        long totalProjects,

        long planningProjects,

        long activeProjects,

        long onHoldProjects,

        long completedProjects,

        long cancelledProjects,

        long archivedProjects
) {
}
