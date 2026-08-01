package com.project.taskmanagement.dto.response.admin;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminImportAuditResponse(

        UUID batchId,

        UUID projectId,

        String projectCode,

        String projectName,

        UUID sprintId,

        String sprintName,

        UUID importedByUserId,

        String importedByUsername,

        String fileName,

        TaskImportStatus status,

        int totalRows,

        int successRows,

        int failedRows,

        Instant startedAt,

        Instant completedAt,

        Instant createdAt
) {
}
