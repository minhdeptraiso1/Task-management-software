package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.report.TimeLogExportRequest;
import com.project.taskmanagement.service.ProjectExcelExportService;
import com.project.taskmanagement.service.model.GeneratedExcelFile;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/exports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectExcelExportController {

    ProjectExcelExportService projectExcelExportService;

    @Operation(summary = "Export Task trong Sprint ra Excel")
    @GetMapping("/sprints/{sprintId}/tasks/excel")
    public ResponseEntity<byte[]> exportSprintTasks(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId
    ) {
        return toResponse(projectExcelExportService.exportSprintTasks(projectId, sprintId));
    }

    @Operation(summary = "Xuất nhật ký thời gian theo khoảng thời gian ra Excel")
    @GetMapping("/time-logs/excel")
    public ResponseEntity<byte[]> exportTimeLogs(
            @PathVariable UUID projectId,
            @ParameterObject TimeLogExportRequest request
    ) {
        return toResponse(projectExcelExportService.exportTimeLogs(projectId, request));
    }

    private ResponseEntity<byte[]> toResponse(GeneratedExcelFile file) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}
