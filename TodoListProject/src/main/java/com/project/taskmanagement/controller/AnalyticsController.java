package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.analytics.BurnupChartResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowResponse;
import com.project.taskmanagement.dto.response.analytics.ProjectAnalyticsSummaryResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.REPORTS, description = "Analytics velocity, burnup và cumulative flow của Project/Sprint")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsController {

    AnalyticsService analyticsService;

    @Operation(summary = "Lấy tổng quan analytics của Project")
    @GetMapping("/projects/{projectId}/analytics/summary")
    public ApiResponseSever<ProjectAnalyticsSummaryResponse> getProjectAnalyticsSummary(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(analyticsService.getProjectAnalyticsSummary(projectId));
    }

    @Operation(summary = "Lấy velocity chart của Project")
    @GetMapping("/projects/{projectId}/analytics/velocity")
    public ApiResponseSever<VelocityChartResponse> getVelocity(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(analyticsService.getVelocity(projectId));
    }

    @Operation(summary = "Lấy burnup chart của Project")
    @GetMapping("/projects/{projectId}/analytics/burnup")
    public ApiResponseSever<BurnupChartResponse> getProjectBurnup(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(analyticsService.getProjectBurnup(projectId));
    }

    @Operation(summary = "Lấy burnup chart của Sprint")
    @GetMapping({
            "/projects/{projectId}/sprints/{sprintId}/analytics/burnup",
            "/projects/{projectId}/analytics/sprints/{sprintId}/burnup"
    })
    public ApiResponseSever<BurnupChartResponse> getSprintBurnup(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return ApiResponseSever.ok(analyticsService.getSprintBurnup(projectId, sprintId));
    }

    @Operation(summary = "Lấy cumulative flow của Sprint")
    @GetMapping({
            "/projects/{projectId}/sprints/{sprintId}/analytics/cumulative-flow",
            "/projects/{projectId}/analytics/sprints/{sprintId}/cumulative-flow"
    })
    public ApiResponseSever<CumulativeFlowResponse> getSprintCumulativeFlow(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return ApiResponseSever.ok(analyticsService.getSprintCumulativeFlow(projectId, sprintId));
    }
}
