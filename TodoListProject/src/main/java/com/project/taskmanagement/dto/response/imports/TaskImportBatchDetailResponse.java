package com.project.taskmanagement.dto.response.imports;

import java.util.List;

public record TaskImportBatchDetailResponse(

        TaskImportBatchResponse batch,

        List<TaskImportErrorResponse> recentErrors
) {
}
