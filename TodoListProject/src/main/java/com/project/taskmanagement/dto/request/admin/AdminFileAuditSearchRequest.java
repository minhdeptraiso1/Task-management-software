package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.SystemAuditAction;

import java.time.LocalDate;
import java.util.UUID;

public record AdminFileAuditSearchRequest(

        UUID actorUserId,

        UUID projectId,

        SystemAuditAction action,

        LocalDate fromDate,

        LocalDate toDate,

        String keyword
) {
}
