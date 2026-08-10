package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.bugreport.BugReportRequest;
import com.project.taskmanagement.dto.response.bugreport.BugDashboardResponse;
import com.project.taskmanagement.dto.response.bugreport.BugReportResponse;
import com.project.taskmanagement.dto.response.bugreport.QaMetricsResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BugReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.REPORTS, description = "Dashboard và chỉ số chất lượng Bug/QA")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugReportController {

    BugReportService bugReportService;

    @Operation(summary = "Lấy Bug Dashboard của Project")
    @GetMapping("/bugs/dashboard")
    public ApiResponseSever<BugDashboardResponse> getProjectBugDashboard(
            @PathVariable UUID projectId
    ) {
        return ApiResponseSever.ok(bugReportService.getProjectBugDashboard(projectId));
    }

    @Operation(summary = "Lấy Bug Report của Project")
    @GetMapping("/bugs/report")
    public ApiResponseSever<BugReportResponse> getProjectBugReport(
            @PathVariable UUID projectId,
            @ParameterObject BugReportRequest request
    ) {
        return ApiResponseSever.ok(bugReportService.getProjectBugReport(projectId, request));
    }

    @Operation(summary = "Lấy Bug Report của Sprint")
    @GetMapping("/sprints/{sprintId}/bugs/report")
    public ApiResponseSever<BugReportResponse> getSprintBugReport(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @ParameterObject BugReportRequest request
    ) {
        return ApiResponseSever.ok(bugReportService.getSprintBugReport(projectId, sprintId, request));
    }

    @Operation(summary = "Lấy QA Metrics của Project")
    @GetMapping("/bugs/metrics/qa")
    public ApiResponseSever<QaMetricsResponse> getQaMetrics(
            @PathVariable UUID projectId,
            @ParameterObject BugReportRequest request
    ) {
        return ApiResponseSever.ok(bugReportService.getQaMetrics(projectId, request));
    }
}
