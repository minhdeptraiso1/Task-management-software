package com.project.taskmanagement.dto.response.dashboard;

public record ProjectDashboardBacklogSummaryResponse(

        long totalItems,

        long productBacklogItems,

        long inSprintItems,

        long draftItems,

        long readyItems,

        long doneItems,

        long cancelledItems,

        long totalStoryPoints

) {
}