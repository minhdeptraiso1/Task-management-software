package com.project.taskmanagement.repository.projection.admin;

import com.project.taskmanagement.enums.TaskImportStatus;

import java.time.Instant;
import java.util.UUID;

public interface AdminImportAuditView {

    UUID getBatchId();

    UUID getProjectId();

    String getProjectCode();

    String getProjectName();

    UUID getSprintId();

    String getSprintName();

    UUID getImportedByUserId();

    String getImportedByUsername();

    String getFileName();

    TaskImportStatus getStatus();

    Integer getTotalRows();

    Integer getSuccessRows();

    Integer getFailedRows();

    Instant getStartedAt();

    Instant getCompletedAt();

    Instant getCreatedAt();
}
