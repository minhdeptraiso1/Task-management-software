package com.project.taskmanagement.repository.projection.taskexport;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface SprintTaskExportRowView {

    UUID getId();

    String getTitle();

    String getDescription();

    TaskType getType();

    TaskStatus getStatus();

    TaskPriority getPriority();

    UUID getProjectId();

    String getProjectCode();

    String getProjectName();

    UUID getSprintId();

    String getSprintName();

    UUID getBacklogItemId();

    String getBacklogItemTitle();

    UUID getAssigneeUserId();

    String getAssigneeUsername();

    String getAssigneeEmail();

    UUID getReporterUserId();

    String getReporterUsername();

    String getReporterEmail();

    Integer getEstimatedMinutes();

    Long getLoggedMinutes();

    LocalDate getStartDate();

    LocalDate getDueDate();

    Instant getCompletedAt();

    Long getPosition();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
