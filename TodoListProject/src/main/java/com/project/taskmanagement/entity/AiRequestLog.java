package com.project.taskmanagement.entity;

import com.project.taskmanagement.ai.AiRequestType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "ai_request_logs")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class AiRequestLog extends BaseAuditEntity {
    @Column(name = "project_id", nullable = false)
    UUID projectId;
    @Column(name = "requested_by_user_id", nullable = false)
    UUID requestedByUserId;
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    AiRequestType requestType;
    @Column(nullable = false, columnDefinition = "TEXT")
    String prompt;
    @Column(columnDefinition = "TEXT")
    String response;
    @Column(nullable = false, length = 50)
    String provider;
    @Column(nullable = false, length = 100)
    String model;
    @Column(nullable = false)
    boolean success;
    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    public void setProjectId(UUID v) {
        projectId = v;
    }

    public void setRequestedByUserId(UUID v) {
        requestedByUserId = v;
    }

    public void setRequestType(AiRequestType v) {
        requestType = v;
    }

    public void setPrompt(String v) {
        prompt = v;
    }

    public void setResponse(String v) {
        response = v;
    }

    public void setProvider(String v) {
        provider = v;
    }

    public void setModel(String v) {
        model = v;
    }

    public void setSuccess(boolean v) {
        success = v;
    }

    public void setErrorMessage(String v) {
        errorMessage = v;
    }
}
