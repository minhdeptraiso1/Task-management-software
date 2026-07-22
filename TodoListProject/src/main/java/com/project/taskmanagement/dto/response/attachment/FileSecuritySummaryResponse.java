package com.project.taskmanagement.dto.response.attachment;

import java.util.List;

public record FileSecuritySummaryResponse(
        long maxFileSizeBytes,
        int maxFilesPerEntity,
        long maxProjectStorageBytes,
        int keepDeletedFileDays,
        List<String> allowedExtensions,
        List<String> blockedExtensions
) {
}
