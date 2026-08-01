package com.project.taskmanagement.dto.response.imports;

import java.time.Instant;
import java.util.UUID;

public record TaskImportErrorResponse(

        UUID id,

        UUID batchId,

        Integer rowNumber,

        String columnName,

        String rawValue,

        String errorMessage,

        Instant createdAt
) {
}
