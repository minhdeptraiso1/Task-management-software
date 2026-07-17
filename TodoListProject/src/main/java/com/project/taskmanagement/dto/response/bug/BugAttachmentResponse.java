package com.project.taskmanagement.dto.response.bug;

import java.time.Instant;
import java.util.UUID;

public record BugAttachmentResponse(
        UUID id,
        UUID bugId,
        UUID uploadedByUserId,
        String uploadedByUsername,
        String originalFileName,
        String contentType,
        Long sizeBytes,
        String downloadUrl,
        boolean canDelete,
        Instant createdAt
) {
}
