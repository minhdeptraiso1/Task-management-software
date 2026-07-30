package com.project.taskmanagement.service.model;

public record GeneratedReportFile(
        String fileName,
        String contentType,
        byte[] content
) {
}
