package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.service.model.GeneratedReportFile;

import java.util.UUID;

public interface ReportExcelExportService {

    GeneratedReportFile exportSprintReport(UUID projectId, UUID sprintId);

    GeneratedReportFile exportProjectReport(UUID projectId, ProjectExcelReportRequest request);
}
