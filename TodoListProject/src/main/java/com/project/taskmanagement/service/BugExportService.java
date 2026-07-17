package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.bug.BugReportExportRequest;
import com.project.taskmanagement.service.model.GeneratedExcelFile;

import java.util.UUID;

public interface BugExportService {

    GeneratedExcelFile exportBugReport(UUID projectId, BugReportExportRequest request);
}
