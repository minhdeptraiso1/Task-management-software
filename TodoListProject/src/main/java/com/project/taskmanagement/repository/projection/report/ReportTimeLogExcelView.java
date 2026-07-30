package com.project.taskmanagement.repository.projection.report;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface ReportTimeLogExcelView {

    UUID getTimeLogId();

    UUID getProjectId();

    String getProjectName();

    UUID getTaskId();

    String getTaskTitle();

    UUID getSprintId();

    String getSprintName();

    UUID getBacklogItemId();

    String getBacklogItemTitle();

    UUID getUserId();

    String getUsername();

    String getEmail();

    LocalDate getWorkDate();

    Integer getMinutes();

    String getDescription();

    Instant getCreatedAt();
}
