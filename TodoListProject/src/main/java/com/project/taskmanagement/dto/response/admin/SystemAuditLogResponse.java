package com.project.taskmanagement.dto.response.admin;

import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;

import java.time.Instant;
import java.util.UUID;

public record SystemAuditLogResponse(

        UUID id,

        UUID actorUserId,

        String actorUsername,

        String actorEmail,

        SystemAuditAction action,

        SystemAuditResourceType resourceType,

        UUID resourceId,

        String ipAddress,

        String userAgent,

        String oldValueJson,

        String newValueJson,

        Boolean success,

        String errorMessage,

        Instant createdAt
) {
}
