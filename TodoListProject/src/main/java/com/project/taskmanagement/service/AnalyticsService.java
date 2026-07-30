package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.analytics.BurnupChartResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowResponse;
import com.project.taskmanagement.dto.response.analytics.ProjectAnalyticsSummaryResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;

import java.util.UUID;

public interface AnalyticsService {

    VelocityChartResponse getVelocity(UUID projectId);

    BurnupChartResponse getProjectBurnup(UUID projectId);

    BurnupChartResponse getSprintBurnup(UUID projectId, UUID sprintId);

    CumulativeFlowResponse getSprintCumulativeFlow(UUID projectId, UUID sprintId);

    ProjectAnalyticsSummaryResponse getProjectAnalyticsSummary(UUID projectId);
}
