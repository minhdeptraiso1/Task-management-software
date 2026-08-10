package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.audit.AuditLogPageResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AuditLogService;
import com.project.taskmanagement.service.validation.PageableValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(
        name = OpenApiTags.AUDIT,
        description = "API quản lý lịch sử hoạt động hệ thống"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogController {

    private static final Set<String> AUDIT_LOG_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "action", "resourceType", "success");

    AuditLogService auditLogService;

    @Operation(
            summary = "Lấy danh sách hoạt động",
            description = "Chỉ ADMIN được xem lịch sử hoạt động hệ thống."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponseSever<AuditLogPageResponse> getAuditLogs(
            @ParameterObject Pageable pageable
    ) {
        PageableValidator.validate(pageable, AUDIT_LOG_SORT_FIELDS);

        return ApiResponseSever.ok(auditLogService.getAuditLogs(pageable));
    }
}
