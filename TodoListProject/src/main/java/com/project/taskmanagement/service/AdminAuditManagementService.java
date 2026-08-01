package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.admin.AdminFileAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.AdminImportAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.AdminAuditSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminImportAuditPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminAuditManagementService {

    SystemAuditLogPageResponse getAuditLogs(
            SystemAuditSearchRequest request,
            Pageable pageable
    );

    SystemAuditLogResponse getAuditLogById(
            UUID auditLogId
    );

    AdminAuditSummaryResponse getAuditSummary(
            SystemAuditSearchRequest request
    );

    AdminImportAuditPageResponse getImportAudits(
            AdminImportAuditSearchRequest request,
            Pageable pageable
    );

    SystemAuditLogPageResponse getFileAudits(
            AdminFileAuditSearchRequest request,
            Pageable pageable
    );

    SystemAuditLogPageResponse getUserActivities(
            UUID userId,
            SystemAuditSearchRequest request,
            Pageable pageable
    );
}
