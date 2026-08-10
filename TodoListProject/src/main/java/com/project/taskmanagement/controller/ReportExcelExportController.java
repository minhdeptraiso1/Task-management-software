package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.controller.support.FileResponseBuilder;
import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.service.ReportExcelExportService;
import com.project.taskmanagement.service.model.GeneratedReportFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.REPORTS, description = "Xuất báo cáo Project và Sprint ra Excel")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportExcelExportController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    ReportExcelExportService reportExcelExportService;
    FileResponseBuilder fileResponseBuilder;

    @Operation(
            summary = "Xuất báo cáo Sprint ra Excel",
            description = "Tạo workbook gồm tổng quan Sprint, Task, Backlog Item, Time Log, hiệu suất thành viên và dữ liệu biểu đồ."
    )
    @ApiResponse(
            responseCode = "200",
            description = "File báo cáo Excel",
            content = @Content(
                    mediaType = EXCEL_CONTENT_TYPE,
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @GetMapping("/projects/{projectId}/sprints/{sprintId}/reports/excel")
    public ResponseEntity<byte[]> exportSprintReport(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        GeneratedReportFile file = reportExcelExportService.exportSprintReport(projectId, sprintId);
        return fileResponseBuilder.build(file);
    }

    @Operation(summary = "Xuất báo cáo Project ra Excel", description = "Xuất dữ liệu tổng hợp Project theo bộ lọc thời gian.")
    @ApiResponse(responseCode = "200", description = "File báo cáo Excel", content = @Content(mediaType = EXCEL_CONTENT_TYPE, schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/projects/{projectId}/reports/excel")
    public ResponseEntity<byte[]> exportProjectReport(
            @PathVariable UUID projectId,
            @ParameterObject ProjectExcelReportRequest request
    ) {
        GeneratedReportFile file = reportExcelExportService.exportProjectReport(projectId, request);
        return fileResponseBuilder.build(file);
    }

    @Operation(summary = "Xuất Time Log của Project ra Excel", description = "Xuất Time Log của thành viên trong Project theo khoảng thời gian và bộ lọc.")
    @ApiResponse(responseCode = "200", description = "File Time Log Excel", content = @Content(mediaType = EXCEL_CONTENT_TYPE, schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/projects/{projectId}/reports/time-logs/excel")
    public ResponseEntity<byte[]> exportProjectTimeLogsReport(
            @PathVariable UUID projectId,
            @ParameterObject ProjectExcelReportRequest request
    ) {
        GeneratedReportFile file = reportExcelExportService.exportProjectTimeLogsReport(projectId, request);
        return fileResponseBuilder.build(file);
    }

    @Operation(summary = "Xuất Time Log của Sprint ra Excel", description = "Xuất Time Log của thành viên thuộc Sprint theo khoảng thời gian.")
    @ApiResponse(responseCode = "200", description = "File Time Log Excel", content = @Content(mediaType = EXCEL_CONTENT_TYPE, schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/projects/{projectId}/sprints/{sprintId}/reports/time-logs/excel")
    public ResponseEntity<byte[]> exportSprintTimeLogsReport(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @ParameterObject ProjectExcelReportRequest request
    ) {
        ProjectExcelReportRequest sprintRequest = new ProjectExcelReportRequest(
                request == null ? null : request.fromDate(),
                request == null ? null : request.toDate(),
                sprintId,
                request == null ? null : request.userId()
        );
        GeneratedReportFile file = reportExcelExportService.exportProjectTimeLogsReport(projectId, sprintRequest);
        return fileResponseBuilder.build(file);
    }

    @Operation(summary = "Xuất Time Log cá nhân ra Excel", description = "Xuất toàn bộ Time Log của người dùng hiện tại theo khoảng thời gian.")
    @ApiResponse(responseCode = "200", description = "File Time Log Excel", content = @Content(mediaType = EXCEL_CONTENT_TYPE, schema = @Schema(type = "string", format = "binary")))
    @GetMapping("/timesheets/me/reports/excel")
    public ResponseEntity<byte[]> exportMyTimeLogsReport(
            @ParameterObject ProjectExcelReportRequest request
    ) {
        GeneratedReportFile file = reportExcelExportService.exportMyTimeLogsReport(request);
        return fileResponseBuilder.build(file);
    }
}
