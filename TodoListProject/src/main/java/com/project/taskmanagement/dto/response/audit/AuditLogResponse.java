package com.project.taskmanagement.dto.response.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String username,
        String email,
        String action,
        Instant createdAt
) {
}
