package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.service.model.GeneratedReportFile;

import java.util.UUID;

public interface ReportPdfExportService {

    GeneratedReportFile exportSprintReportPdf(UUID projectId, UUID sprintId);

    GeneratedReportFile exportProjectReportPdf(UUID projectId, ProjectExcelReportRequest request);
}
