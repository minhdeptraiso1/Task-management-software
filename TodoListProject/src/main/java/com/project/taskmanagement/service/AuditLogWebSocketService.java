package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.AuditLog;

public interface AuditLogWebSocketService {
    void push(AuditLog log);
}

