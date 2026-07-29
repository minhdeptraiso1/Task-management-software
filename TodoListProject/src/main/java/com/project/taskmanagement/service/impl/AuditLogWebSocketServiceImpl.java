package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.audit.AuditLogResponse;
import com.project.taskmanagement.entity.AuditLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.AuditLogWebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogWebSocketServiceImpl
        implements AuditLogWebSocketService {

    SimpMessagingTemplate messagingTemplate;
    UserRepository userRepository;

    @Override
    public void push(AuditLog log) {
        User user = userRepository.findById(log.getUserId()).orElse(null);
        AuditLogResponse response = new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                log.getAction(),
                log.getCreatedAt()
        );
        messagingTemplate.convertAndSend("/topic/audit-logs", response);
    }
}
