package com.project.taskmanagement.dto.request.imports;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TaskImportHistorySearchRequest(

        TaskImportStatus status,

        UUID sprintId,

        UUID importedByUserId,

        LocalDate fromDate,

        LocalDate toDate
) {
}
