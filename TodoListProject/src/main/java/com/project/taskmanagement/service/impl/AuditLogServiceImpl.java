package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.audit.AuditLogPageResponse;
import com.project.taskmanagement.dto.response.audit.AuditLogResponse;
import com.project.taskmanagement.entity.AuditLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.AuditLogRepository;
import com.project.taskmanagement.repository.UserRepository;
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
    UserRepository userRepository;
    AuditLogWebSocketService webSocketService;

    @Override
    public void log(UUID userId, String action) {

        AuditLog log = AuditLog.builder()
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
        User user = userRepository.findById(log.getUserId()).orElse(null);
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                log.getAction(),
                log.getCreatedAt()
        );
    }
}
