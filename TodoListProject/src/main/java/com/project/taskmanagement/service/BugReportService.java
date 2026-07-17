package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.bugreport.BugReportRequest;
import com.project.taskmanagement.dto.response.bugreport.BugDashboardResponse;
import com.project.taskmanagement.dto.response.bugreport.BugReportResponse;
import com.project.taskmanagement.dto.response.bugreport.QaMetricsResponse;

import java.util.UUID;

public interface BugReportService {

    BugDashboardResponse getProjectBugDashboard(UUID projectId);

    BugReportResponse getProjectBugReport(UUID projectId, BugReportRequest request);

    BugReportResponse getSprintBugReport(UUID projectId, UUID sprintId, BugReportRequest request);

    QaMetricsResponse getQaMetrics(UUID projectId, BugReportRequest request);
}
