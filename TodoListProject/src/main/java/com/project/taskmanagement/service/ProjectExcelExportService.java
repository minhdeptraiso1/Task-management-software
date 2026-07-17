package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.report.TimeLogExportRequest;
import com.project.taskmanagement.service.model.GeneratedExcelFile;

import java.util.UUID;

public interface ProjectExcelExportService {

    GeneratedExcelFile exportSprintTasks(UUID projectId, UUID sprintId);

    GeneratedExcelFile exportTimeLogs(UUID projectId, TimeLogExportRequest request);
}
