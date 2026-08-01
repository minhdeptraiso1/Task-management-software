package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.admin.AdminFileAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.AdminImportAuditSearchRequest;
import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.AdminAuditSummaryResponse;
import com.project.taskmanagement.dto.response.admin.AdminImportAuditPageResponse;
import com.project.taskmanagement.dto.response.admin.AdminImportAuditResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import com.project.taskmanagement.entity.SystemAuditLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.SystemAuditLogRepository;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.repository.projection.admin.AdminImportAuditView;
import com.project.taskmanagement.repository.spec.SystemAuditLogSpecification;
import com.project.taskmanagement.service.AdminAuditManagementService;
import com.project.taskmanagement.service.validation.AdminPermissionValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminAuditManagementServiceImpl
        implements AdminAuditManagementService {

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DEFAULT_RANGE_DAYS = 30;

    SystemAuditLogRepository systemAuditLogRepository;
    TaskImportBatchRepository taskImportBatchRepository;
    UserRepository userRepository;
    AdminPermissionValidator adminPermissionValidator;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_AUDIT_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|request=' + T(java.lang.String).valueOf(#request)" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public SystemAuditLogPageResponse getAuditLogs(
            SystemAuditSearchRequest request,
            Pageable pageable
    ) {
        adminPermissionValidator.requireAdmin();
        validateDateRange(request);

        Page<SystemAuditLogResponse> page =
                systemAuditLogRepository
                        .findAll(
                                SystemAuditLogSpecification.byRequest(request),
                                pageable
                        )
                        .map(this::toAuditResponse);

        return SystemAuditLogPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_AUDIT_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|audit=' + #auditLogId"
    )
    public SystemAuditLogResponse getAuditLogById(
            UUID auditLogId
    ) {
        adminPermissionValidator.requireAdmin();

        SystemAuditLog auditLog =
                systemAuditLogRepository
                        .findById(auditLogId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SYSTEM_AUDIT_LOG_NOT_FOUND
                                )
                        );

        return toAuditResponse(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_AUDIT_SUMMARY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|request=' + T(java.lang.String).valueOf(#request)"
    )
    public AdminAuditSummaryResponse getAuditSummary(
            SystemAuditSearchRequest request
    ) {
        adminPermissionValidator.requireAdmin();
        validateDateRange(request);

        SystemAuditSearchRequest resolvedRequest =
                withDefaultDateRange(request);

        Specification<SystemAuditLog> specification =
                SystemAuditLogSpecification.byRequest(resolvedRequest);

        var logs =
                systemAuditLogRepository.findAll(specification);

        var byAction =
                logs.stream()
                        .collect(Collectors.groupingBy(
                                log -> log.getAction().name(),
                                Collectors.counting()
                        ))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(entry ->
                                new AdminAuditSummaryResponse.AdminAuditSummaryItemResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        var byResourceType =
                logs.stream()
                        .collect(Collectors.groupingBy(
                                log -> log.getResourceType().name(),
                                Collectors.counting()
                        ))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(entry ->
                                new AdminAuditSummaryResponse.AdminAuditSummaryItemResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        var byDate =
                logs.stream()
                        .collect(Collectors.groupingBy(
                                log -> LocalDate.ofInstant(
                                        log.getCreatedAt(),
                                        BUSINESS_ZONE
                                ),
                                Collectors.counting()
                        ))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(entry ->
                                new AdminAuditSummaryResponse.AdminAuditDailySummaryResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        return new AdminAuditSummaryResponse(
                logs.size(),
                countByResourceType(logs, SystemAuditResourceType.AUTH),
                countByResourceType(logs, SystemAuditResourceType.USER),
                countByResourceType(logs, SystemAuditResourceType.PROJECT),
                countFileLogs(logs),
                countImportLogs(logs),
                byAction,
                byResourceType,
                byDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_IMPORT_AUDIT_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|request=' + T(java.lang.String).valueOf(#request)" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public AdminImportAuditPageResponse getImportAudits(
            AdminImportAuditSearchRequest request,
            Pageable pageable
    ) {
        adminPermissionValidator.requireAdmin();
        validateDateRange(request);

        DateTimeRange range =
                resolveDateTimeRange(
                        request == null ? null : request.fromDate(),
                        request == null ? null : request.toDate(),
                        false
                );

        Page<AdminImportAuditResponse> page =
                taskImportBatchRepository
                        .searchAdminImportAudits(
                                request == null ? null : request.projectId(),
                                request == null ? null : request.importedByUserId(),
                                request == null ? null : request.status(),
                                range.from(),
                                range.to(),
                                normalizeKeyword(
                                        request == null
                                                ? null
                                                : request.keyword()
                                ),
                                pageable
                        )
                        .map(this::toImportAuditResponse);

        return AdminImportAuditPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_FILE_AUDIT_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|request=' + T(java.lang.String).valueOf(#request)" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public SystemAuditLogPageResponse getFileAudits(
            AdminFileAuditSearchRequest request,
            Pageable pageable
    ) {
        adminPermissionValidator.requireAdmin();
        validateDateRange(request);

        Specification<SystemAuditLog> attachmentSpecification =
                SystemAuditLogSpecification.byRequest(
                        new SystemAuditSearchRequest(
                                request == null ? null : request.actorUserId(),
                                request == null ? null : request.action(),
                                SystemAuditResourceType.ATTACHMENT,
                                null,
                                null,
                                request == null ? null : request.fromDate(),
                                request == null ? null : request.toDate(),
                                request == null ? null : request.keyword()
                        )
                );

        Specification<SystemAuditLog> legacyFileSpecification =
                SystemAuditLogSpecification.byRequest(
                        new SystemAuditSearchRequest(
                                request == null ? null : request.actorUserId(),
                                request == null ? null : request.action(),
                                SystemAuditResourceType.FILE,
                                null,
                                null,
                                request == null ? null : request.fromDate(),
                                request == null ? null : request.toDate(),
                                request == null ? null : request.keyword()
                        )
                );

        Specification<SystemAuditLog> specification =
                attachmentSpecification.or(legacyFileSpecification);

        UUID projectId =
                request == null
                        ? null
                        : request.projectId();

        if (projectId != null) {
            specification =
                    specification.and(jsonContains(projectId.toString()));
        }

        Page<SystemAuditLogResponse> page =
                systemAuditLogRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toAuditResponse);

        return SystemAuditLogPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.ADMIN_USER_ACTIVITY_AUDIT,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|user=' + #userId" +
                    " + '|request=' + T(java.lang.String).valueOf(#request)" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public SystemAuditLogPageResponse getUserActivities(
            UUID userId,
            SystemAuditSearchRequest request,
            Pageable pageable
    ) {
        adminPermissionValidator.requireAdmin();
        validateDateRange(request);

        userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        SystemAuditSearchRequest resolvedRequest =
                newAuditRequest(request, userId);

        Page<SystemAuditLogResponse> page =
                systemAuditLogRepository
                        .findAll(
                                SystemAuditLogSpecification.byRequest(resolvedRequest),
                                pageable
                        )
                        .map(this::toAuditResponse);

        return SystemAuditLogPageResponse.from(page);
    }

    private Specification<SystemAuditLog> jsonContains(
            String value
    ) {
        return (root, query, cb) -> {
            if (value == null
                    || value.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%"
                            + value
                            .trim()
                            .toLowerCase()
                            + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("oldValueJson")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("newValueJson")),
                            pattern
                    )
            );
        };
    }

    private SystemAuditSearchRequest newAuditRequest(
            SystemAuditSearchRequest request,
            UUID actorUserId
    ) {
        return new SystemAuditSearchRequest(
                actorUserId,
                request == null ? null : request.action(),
                request == null ? null : request.resourceType(),
                request == null ? null : request.resourceId(),
                request == null ? null : request.success(),
                request == null ? null : request.fromDate(),
                request == null ? null : request.toDate(),
                request == null ? null : request.keyword()
        );
    }

    private SystemAuditLogResponse toAuditResponse(
            SystemAuditLog auditLog
    ) {
        User actor =
                auditLog.getActorUserId() == null
                        ? null
                        : userRepository
                        .findById(auditLog.getActorUserId())
                        .orElse(null);

        return new SystemAuditLogResponse(
                auditLog.getId(),
                auditLog.getActorUserId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getEmail(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getOldValueJson(),
                auditLog.getNewValueJson(),
                auditLog.getSuccess(),
                auditLog.getErrorMessage(),
                auditLog.getCreatedAt()
        );
    }

    private AdminImportAuditResponse toImportAuditResponse(
            AdminImportAuditView view
    ) {
        return new AdminImportAuditResponse(
                view.getBatchId(),
                view.getProjectId(),
                view.getProjectCode(),
                view.getProjectName(),
                view.getSprintId(),
                view.getSprintName(),
                view.getImportedByUserId(),
                view.getImportedByUsername(),
                view.getFileName(),
                view.getStatus(),
                safeInt(view.getTotalRows()),
                safeInt(view.getSuccessRows()),
                safeInt(view.getFailedRows()),
                view.getStartedAt(),
                view.getCompletedAt(),
                view.getCreatedAt()
        );
    }

    private SystemAuditSearchRequest withDefaultDateRange(
            SystemAuditSearchRequest request
    ) {
        if (request != null
                && request.fromDate() != null
                && request.toDate() != null) {
            return request;
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        return new SystemAuditSearchRequest(
                request == null ? null : request.actorUserId(),
                request == null ? null : request.action(),
                request == null ? null : request.resourceType(),
                request == null ? null : request.resourceId(),
                request == null ? null : request.success(),
                request == null || request.fromDate() == null
                        ? today.minusDays(DEFAULT_RANGE_DAYS - 1L)
                        : request.fromDate(),
                request == null || request.toDate() == null
                        ? today
                        : request.toDate(),
                request == null ? null : request.keyword()
        );
    }

    private DateTimeRange resolveDateTimeRange(
            LocalDate fromDate,
            LocalDate toDate,
            boolean withDefaultRange
    ) {
        if (!withDefaultRange
                && fromDate == null
                && toDate == null) {
            return new DateTimeRange(null, null);
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        LocalDate resolvedFromDate =
                fromDate == null
                        ? today.minusDays(DEFAULT_RANGE_DAYS - 1L)
                        : fromDate;

        LocalDate resolvedToDate =
                toDate == null
                        ? today
                        : toDate;

        if (resolvedFromDate.isAfter(resolvedToDate)) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_DATE_RANGE_INVALID
            );
        }

        Instant from =
                resolvedFromDate
                        .atStartOfDay(BUSINESS_ZONE)
                        .toInstant();

        Instant to =
                resolvedToDate
                        .atTime(LocalTime.MAX)
                        .atZone(BUSINESS_ZONE)
                        .toInstant();

        return new DateTimeRange(from, to);
    }

    private void validateDateRange(
            SystemAuditSearchRequest request
    ) {
        if (request == null
                || request.fromDate() == null
                || request.toDate() == null) {
            return;
        }

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_DATE_RANGE_INVALID
            );
        }
    }

    private void validateDateRange(
            AdminImportAuditSearchRequest request
    ) {
        if (request == null
                || request.fromDate() == null
                || request.toDate() == null) {
            return;
        }

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_DATE_RANGE_INVALID
            );
        }
    }

    private void validateDateRange(
            AdminFileAuditSearchRequest request
    ) {
        if (request == null
                || request.fromDate() == null
                || request.toDate() == null) {
            return;
        }

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_DATE_RANGE_INVALID
            );
        }
    }

    private String normalizeKeyword(
            String keyword
    ) {
        return keyword == null
                || keyword.isBlank()
                ? null
                : keyword.trim();
    }

    private long countByResourceType(
            Iterable<SystemAuditLog> logs,
            SystemAuditResourceType resourceType
    ) {
        long count = 0;
        for (SystemAuditLog log : logs) {
            if (Objects.equals(log.getResourceType(), resourceType)) {
                count++;
            }
        }
        return count;
    }

    private long countFileLogs(
            Iterable<SystemAuditLog> logs
    ) {
        long count = 0;
        for (SystemAuditLog log : logs) {
            if (log.getResourceType() == SystemAuditResourceType.FILE
                    || log.getResourceType() == SystemAuditResourceType.ATTACHMENT) {
                count++;
            }
        }
        return count;
    }

    private long countImportLogs(
            Iterable<SystemAuditLog> logs
    ) {
        long count = 0;
        for (SystemAuditLog log : logs) {
            if (log.getResourceType() == SystemAuditResourceType.IMPORT_BATCH
                    || log.getResourceType() == SystemAuditResourceType.TASK_IMPORT_BATCH) {
                count++;
            }
        }
        return count;
    }

    private int safeInt(
            Number number
    ) {
        return number == null
                ? 0
                : number.intValue();
    }

    private record DateTimeRange(
            Instant from,
            Instant to
    ) {
    }
}
