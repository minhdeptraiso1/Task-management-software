package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.SystemAuditAction;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AdminFileAuditSearchRequest(

        UUID actorUserId,

        UUID projectId,

        SystemAuditAction action,

        LocalDate fromDate,

        LocalDate toDate,

        @Size(max = 150, message = "Từ khóa file audit không được vượt quá 150 ký tự")
        String keyword
) {
}
