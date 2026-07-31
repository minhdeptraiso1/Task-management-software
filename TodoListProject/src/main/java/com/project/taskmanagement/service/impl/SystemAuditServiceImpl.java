package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogPageResponse;
import com.project.taskmanagement.dto.response.admin.SystemAuditLogResponse;
import com.project.taskmanagement.entity.SystemAuditLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.SystemAuditLogRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.repository.spec.SystemAuditLogSpecification;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SystemAuditServiceImpl
        implements SystemAuditService {

    SystemAuditLogRepository systemAuditLogRepository;
    UserRepository userRepository;
    ObjectMapper objectMapper;

    static final Set<String> SENSITIVE_KEYS =
            Set.of(
                    "password",
                    "token",
                    "accessToken",
                    "refreshToken",
                    "secret",
                    "authorization"
            );

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ADMIN_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.ADMIN_AUDIT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.ADMIN_AUDIT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.ADMIN_AUDIT_SUMMARY, allEntries = true),
            @CacheEvict(value = CacheNames.ADMIN_FILE_AUDIT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.ADMIN_USER_ACTIVITY_AUDIT, allEntries = true)
    })
    public void log(
            SystemAuditCommand command
    ) {
        if (command == null
                || command.action() == null
                || command.resourceType() == null) {
            return;
        }

        try {
            SystemAuditLog auditLog =
                    SystemAuditLog
                            .builder()
                            .actorUserId(command.actorUserId())
                            .action(command.action())
                            .resourceType(command.resourceType())
                            .resourceId(command.resourceId())
                            .ipAddress(command.ipAddress())
                            .userAgent(command.userAgent())
                            .oldValueJson(toJson(command.oldValue()))
                            .newValueJson(toJson(command.newValue()))
                            .success(command.success() == null
                                    || command.success())
                            .errorMessage(command.errorMessage())
                            .build();

            systemAuditLogRepository.save(auditLog);
        } catch (RuntimeException ignored) {
            /*
             * Audit lÃ  luá»“ng phá»¥. KhÃ´ng Ä‘á»ƒ lá»—i ghi audit
             * lÃ m há»ng business chÃ­nh nhÆ° login/disable/download.
             */
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogPageResponse search(
            SystemAuditSearchRequest request,
            Pageable pageable
    ) {
        Page<SystemAuditLogResponse> page =
                systemAuditLogRepository
                        .findAll(
                                SystemAuditLogSpecification.byRequest(request),
                                pageable
                        )
                        .map(this::toResponse);

        return SystemAuditLogPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogResponse getById(
            UUID auditLogId
    ) {
        SystemAuditLog auditLog =
                systemAuditLogRepository
                        .findById(auditLogId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SYSTEM_AUDIT_LOG_NOT_FOUND
                                )
                        );

        return toResponse(auditLog);
    }

    private SystemAuditLogResponse toResponse(
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

    private String toJson(
            Map<String, Object> value
    ) {
        if (value == null) {
            return null;
        }

        Map<String, Object> sanitized =
                new LinkedHashMap<>();

        value.forEach((key, itemValue) -> {
            if (key == null) {
                return;
            }

            boolean sensitive =
                    SENSITIVE_KEYS
                            .stream()
                            .anyMatch(sensitiveKey ->
                                    key.toLowerCase()
                                            .contains(
                                                    sensitiveKey.toLowerCase()
                                            )
                            );

            sanitized.put(
                    key,
                    sensitive ? "***" : itemValue
            );
        });

        try {
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
