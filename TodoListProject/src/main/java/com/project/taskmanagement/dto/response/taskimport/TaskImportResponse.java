package com.project.taskmanagement.dto.response.taskimport;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.util.List;
import java.util.UUID;

public record TaskImportResponse(

        UUID importBatchId,

        TaskImportStatus status,

        int totalRows,

        int successRows,

        int failedRows,

        List<UUID> importedTaskIds,

        List<TaskImportErrorResponse> errors

) {

    public static TaskImportResponse validationFailed(
            UUID batchId,
            int totalRows,
            List<TaskImportErrorResponse> errors
    ) {
        return new TaskImportResponse(
                batchId,
                TaskImportStatus.VALIDATION_FAILED,
                totalRows,
                0,
                errors.size(),
                List.of(),
                errors
        );
    }

    public static TaskImportResponse completed(
            UUID batchId,
            int totalRows,
            List<UUID> taskIds
    ) {
        return new TaskImportResponse(
                batchId,
                TaskImportStatus.COMPLETED,
                totalRows,
                taskIds.size(),
                0,
                taskIds,
                List.of()
        );
    }
}