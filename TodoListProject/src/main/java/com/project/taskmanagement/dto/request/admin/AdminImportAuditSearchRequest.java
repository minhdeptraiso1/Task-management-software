package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.TaskImportStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AdminImportAuditSearchRequest(

        UUID projectId,

        UUID importedByUserId,

        TaskImportStatus status,

        LocalDate fromDate,

        LocalDate toDate,

        @Size(max = 150, message = "Từ khóa import audit không được vượt quá 150 ký tự")
        String keyword
) {
}
