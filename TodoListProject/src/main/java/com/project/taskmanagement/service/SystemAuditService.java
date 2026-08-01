package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SystemAuditService {

    void log(
            SystemAuditCommand command
    );

    SystemAuditLogPageResponse search(
            SystemAuditSearchRequest request,
            Pageable pageable
    );

    SystemAuditLogResponse getById(
            UUID auditLogId
    );
}
