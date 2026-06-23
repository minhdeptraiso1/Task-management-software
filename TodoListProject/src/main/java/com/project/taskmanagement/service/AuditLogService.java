package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.audit.AuditLogPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditLogService {
    void log(UUID userId, String action);

    AuditLogPageResponse getAuditLogs(Pageable pageable);
}
