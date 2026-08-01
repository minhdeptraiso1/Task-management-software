package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AdminImportAuditSearchRequest(

        UUID projectId,

        UUID importedByUserId,

        TaskImportStatus status,

        LocalDate fromDate,

        LocalDate toDate,

        String keyword
) {
}
