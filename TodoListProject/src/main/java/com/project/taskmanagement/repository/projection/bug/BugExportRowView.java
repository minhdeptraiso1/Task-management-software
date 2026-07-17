package com.project.taskmanagement.repository.projection.bug;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface BugExportRowView {

    UUID getId();

    String getTitle();

    String getDescription();

    BugStatus getStatus();

    BugSeverity getSeverity();

    TaskPriority getPriority();

    UUID getProjectId();

    String getProjectCode();

    String getProjectName();

    UUID getSprintId();

    String getSprintName();

    UUID getTaskId();

    String getTaskTitle();

    UUID getBacklogItemId();

    String getBacklogItemTitle();

    UUID getAssigneeUserId();

    String getAssigneeUsername();

    String getAssigneeEmail();

    UUID getReporterUserId();

    String getReporterUsername();

    String getReporterEmail();

    LocalDate getDueDate();

    Integer getReopenedCount();

    Instant getResolvedAt();

    Instant getClosedAt();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
