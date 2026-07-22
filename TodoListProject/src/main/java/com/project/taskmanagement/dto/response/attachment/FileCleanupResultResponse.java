package com.project.taskmanagement.dto.response.attachment;

public record FileCleanupResultResponse(
        int scannedFiles,
        int deletedFiles,
        int failedFiles
) {
}
