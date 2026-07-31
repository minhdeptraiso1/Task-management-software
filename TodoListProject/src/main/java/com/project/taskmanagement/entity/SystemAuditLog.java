package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemAuditLog extends BaseIdEntity {

    @Column(name = "actor_user_id")
    UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 100)
    SystemAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 100)
    SystemAuditResourceType resourceType;

    @Column(name = "resource_id")
    UUID resourceId;

    @Column(name = "ip_address", length = 100)
    String ipAddress;

    @Column(name = "user_agent", length = 1000)
    String userAgent;

    @Column(name = "old_value_json", columnDefinition = "TEXT")
    String oldValueJson;

    @Column(name = "new_value_json", columnDefinition = "TEXT")
    String newValueJson;

    @Builder.Default
    @Column(name = "success", nullable = false)
    Boolean success = true;

    @Column(name = "error_message", length = 2000)
    String errorMessage;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    Instant createdAt = Instant.now();
}
