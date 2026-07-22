package com.project.taskmanagement.service.model;

public record StoredFile(
        String originalFileName,
        String storedFileName,
        String contentType,
        String extension,
        long sizeBytes,
        String storagePath
) {
}
