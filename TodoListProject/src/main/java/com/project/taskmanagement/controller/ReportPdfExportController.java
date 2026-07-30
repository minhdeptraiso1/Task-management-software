package com.project.taskmanagement.controller;

import com.project.taskmanagement.controller.support.FileResponseBuilder;
import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.service.ReportPdfExportService;
import com.project.taskmanagement.service.model.GeneratedReportFile;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportPdfExportController {

    ReportPdfExportService reportPdfExportService;
    FileResponseBuilder fileResponseBuilder;

    @Operation(summary = "Xuất báo cáo Sprint ra PDF")
    @GetMapping("/projects/{projectId}/sprints/{sprintId}/reports/pdf")
    public ResponseEntity<byte[]> exportSprintReportPdf(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        GeneratedReportFile file = reportPdfExportService.exportSprintReportPdf(projectId, sprintId);
        return fileResponseBuilder.build(file);
    }

    @Operation(summary = "Xuất báo cáo Project ra PDF")
    @GetMapping("/projects/{projectId}/reports/pdf")
    public ResponseEntity<byte[]> exportProjectReportPdf(
            @PathVariable UUID projectId,
            @ParameterObject ProjectExcelReportRequest request
    ) {
        GeneratedReportFile file = reportPdfExportService.exportProjectReportPdf(projectId, request);
        return fileResponseBuilder.build(file);
    }
}
