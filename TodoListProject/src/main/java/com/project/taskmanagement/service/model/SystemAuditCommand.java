package com.project.taskmanagement.service.model;

import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;

import java.util.Map;
import java.util.UUID;

public record SystemAuditCommand(

        UUID actorUserId,

        SystemAuditAction action,

        SystemAuditResourceType resourceType,

        UUID resourceId,

        String ipAddress,

        String userAgent,

        Map<String, Object> oldValue,

        Map<String, Object> newValue,

        Boolean success,

        String errorMessage
) {
}
