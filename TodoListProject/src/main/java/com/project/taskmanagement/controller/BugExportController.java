package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.bug.BugReportExportRequest;
import com.project.taskmanagement.service.BugExportService;
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
@RequestMapping("/projects/{projectId}/bugs/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugExportController {

    BugExportService bugExportService;

    @Operation(summary = "Xuất báo cáo lỗi Bug/QA ra Excel")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportBugReportExcel(
            @PathVariable UUID projectId,
            @ParameterObject BugReportExportRequest request
    ) {
        return toResponse(bugExportService.exportBugReport(projectId, request));
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
