package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;

import java.time.LocalDate;
import java.util.UUID;

public record SystemAuditSearchRequest(

        UUID actorUserId,

        SystemAuditAction action,

        SystemAuditResourceType resourceType,

        UUID resourceId,

        Boolean success,

        LocalDate fromDate,

        LocalDate toDate,

        String keyword
) {
}
