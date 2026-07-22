package com.project.taskmanagement.service.model;

import org.springframework.core.io.Resource;

public record LoadedFile(
        Resource resource,
        String originalFileName,
        String contentType,
        long sizeBytes
) {
}
