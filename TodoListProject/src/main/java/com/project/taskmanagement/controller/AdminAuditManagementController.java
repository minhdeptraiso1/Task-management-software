package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.admin.AdminFileAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.AdminImportAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.AdminAuditSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminImportAuditPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AdminAuditManagementService;
import com.project.taskmanagement.service.validation.DateRangeValidator;
import com.project.taskmanagement.service.validation.PageableValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.Set;
import java.util.UUID;

@Tag(name = OpenApiTags.AUDIT, description = "Tra cứu System Audit, Import Audit và tổng hợp audit dành cho ADMIN")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminAuditManagementController {

    private static final Set<String> AUDIT_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "actorUserId", "action", "resourceType", "resourceId", "success");

    private static final Set<String> IMPORT_AUDIT_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "status", "originalFileName", "totalRows", "successRows", "failedRows", "startedAt", "completedAt");

    AdminAuditManagementService adminAuditManagementService;

    @Operation(summary = "Danh sach system audit log")
    @GetMapping("/audit-logs")
    public ApiResponseSever<SystemAuditLogPageResponse> getAuditLogs(
            @Valid
            @ParameterObject
            SystemAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        PageableValidator.validate(pageable, AUDIT_SORT_FIELDS);

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
            @Valid
            @ParameterObject
            SystemAuditSearchRequest request
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());

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
            @Valid
            @ParameterObject
            AdminImportAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        PageableValidator.validate(pageable, IMPORT_AUDIT_SORT_FIELDS);

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
            @Valid
            @ParameterObject
            AdminFileAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        PageableValidator.validate(pageable, AUDIT_SORT_FIELDS);

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

            @Valid
            @ParameterObject
            SystemAuditSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        PageableValidator.validate(pageable, AUDIT_SORT_FIELDS);

        return ApiResponseSever.ok(
                adminAuditManagementService.getUserActivities(
                        userId,
                        request,
                        pageable
                )
        );
    }
}
