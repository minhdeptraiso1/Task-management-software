package com.project.taskmanagement.dto.response.dashboard;

import java.util.List;

public record ProjectDashboardResponse(

        ProjectDashboardProjectResponse project,

        ProjectDashboardSprintResponse currentSprint,

        ProjectDashboardBacklogSummaryResponse backlogSummary,

        ProjectDashboardTaskSummaryResponse taskSummary,

        List<ProjectDashboardMemberWorkloadResponse> workload,

        List<ProjectDashboardActivityResponse> recentActivities

) {
}