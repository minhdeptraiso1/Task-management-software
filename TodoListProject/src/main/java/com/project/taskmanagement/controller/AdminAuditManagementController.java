package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.admin.AdminFileAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.AdminImportAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.AdminAuditSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminImportAuditPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AdminAuditManagementService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminAuditManagementController {

    AdminAuditManagementService adminAuditManagementService;

    @Operation(summary = "Danh sach system audit log")
    @GetMapping("/audit-logs")
    public ApiResponseSever<SystemAuditLogPageResponse> getAuditLogs(
            @ParameterObject
            SystemAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getAuditLogs(
                        request,
                        pageable
                )
        );
    }

    @Operation(summary = "Tong hop system audit log")
    @GetMapping("/audit-logs/summary")
    public ApiResponseSever<AdminAuditSummaryResponse> getAuditSummary(
            @ParameterObject
            SystemAuditSearchRequest request
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getAuditSummary(request)
        );
    }

    @Operation(summary = "Chi tiet system audit log")
    @GetMapping("/audit-logs/{auditLogId}")
    public ApiResponseSever<SystemAuditLogResponse> getAuditLogById(
            @PathVariable
            UUID auditLogId
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getAuditLogById(auditLogId)
        );
    }

    @Operation(summary = "Danh sach import audit")
    @GetMapping("/import-audits")
    public ApiResponseSever<AdminImportAuditPageResponse> getImportAudits(
            @ParameterObject
            AdminImportAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getImportAudits(
                        request,
                        pageable
                )
        );
    }

    @Operation(summary = "Danh sach file audit")
    @GetMapping("/file-audits")
    public ApiResponseSever<SystemAuditLogPageResponse> getFileAudits(
            @ParameterObject
            AdminFileAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getFileAudits(
                        request,
                        pageable
                )
        );
    }

    @Operation(summary = "Danh sach hoat dong he thong cua user")
    @GetMapping("/users/{userId}/activities")
    public ApiResponseSever<SystemAuditLogPageResponse> getUserActivities(
            @PathVariable
            UUID userId,

            @ParameterObject
            SystemAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                adminAuditManagementService.getUserActivities(
                        userId,
                        request,
                        pageable
                )
        );
    }
}
