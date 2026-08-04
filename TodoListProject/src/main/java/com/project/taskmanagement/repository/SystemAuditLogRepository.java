package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.SystemAuditLog;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SystemAuditLogRepository
        extends JpaRepository<SystemAuditLog, UUID>,
        JpaSpecificationExecutor<SystemAuditLog> {

    long countByResourceType(
            SystemAuditResourceType resourceType
    );

}
