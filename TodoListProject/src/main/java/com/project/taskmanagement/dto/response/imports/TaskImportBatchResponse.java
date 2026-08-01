package com.project.taskmanagement.dto.response.imports;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskImportBatchResponse(

        UUID id,

        UUID projectId,

        UUID sprintId,

        String sprintName,

        String fileName,

        TaskImportStatus status,

        Integer totalRows,

        Integer successRows,

        Integer failedRows,

        UUID importedByUserId,

        String importedByUsername,

        String importedByEmail,

        Instant startedAt,

        Instant completedAt,

        Instant createdAt
) {
}
