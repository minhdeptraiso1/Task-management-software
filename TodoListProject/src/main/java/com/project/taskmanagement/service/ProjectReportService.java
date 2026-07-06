package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.report.ProjectMemberReportRequest;
import com.project.taskmanagement.dto.request.report.ProjectTimeReportRequest;
import com.project.taskmanagement.dto.response.report.ProjectMemberReportResponse;
import com.project.taskmanagement.dto.response.report.ProjectTimeReportResponse;
import com.project.taskmanagement.dto.response.report.SprintReportResponse;

import java.util.UUID;

public interface ProjectReportService {

    SprintReportResponse getSprintReport(
            UUID projectId,
            UUID sprintId
    );

    ProjectMemberReportResponse getMemberReport(
            UUID projectId,
            ProjectMemberReportRequest request
    );

    ProjectTimeReportResponse getTimeReport(
            UUID projectId,
            ProjectTimeReportRequest request
    );
}