package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.audit.AuditLogPageResponse;
import com.project.taskmanagement.dto.response.audit.AuditLogResponse;
import com.project.taskmanagement.entity.AuditLog;
import com.project.taskmanagement.repository.AuditLogRepository;
import com.project.taskmanagement.service.AuditLogService;
import com.project.taskmanagement.service.AuditLogWebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogServiceImpl implements AuditLogService {

    AuditLogRepository auditLogRepository;
    AuditLogWebSocketService webSocketService;

    @Override
    public void log(UUID userId, String action) {

        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action(action)
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(log);

        // push realtime
        webSocketService.push(log);
    }

    @Override
    public AuditLogPageResponse getAuditLogs(Pageable pageable) {
        return AuditLogPageResponse.from(
                auditLogRepository
                        .findAll(pageable)
                        .map(this::toResponse)
        );
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getAction(),
                log.getCreatedAt()
        );
    }
}
