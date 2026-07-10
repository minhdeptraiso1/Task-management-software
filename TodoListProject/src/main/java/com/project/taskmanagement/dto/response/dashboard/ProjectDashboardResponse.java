package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.dto.response.taskrisk.TaskRiskSummaryResponse;

import java.util.List;

public record ProjectDashboardResponse(

        ProjectDashboardProjectResponse project,

        ProjectDashboardSprintResponse currentSprint,

        ProjectDashboardBacklogSummaryResponse backlogSummary,

        ProjectDashboardTaskSummaryResponse taskSummary,

        TaskRiskSummaryResponse riskSummary,

        List<ProjectDashboardMemberWorkloadResponse> workload,

        List<ProjectDashboardActivityResponse> recentActivities

) {
}
