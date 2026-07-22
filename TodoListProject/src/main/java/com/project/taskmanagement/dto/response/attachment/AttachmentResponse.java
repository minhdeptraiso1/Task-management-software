package com.project.taskmanagement.dto.response.attachment;

import com.project.taskmanagement.enums.AttachmentEntityType;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID projectId,
        AttachmentEntityType entityType,
        UUID entityId,
        UUID uploadedByUserId,
        String uploadedByUsername,
        String uploadedByEmail,
        String originalFileName,
        String contentType,
        String extension,
        Long sizeBytes,
        String downloadUrl,
        boolean canDelete,
        Instant createdAt
) {
}
