package com.project.taskmanagement.repository.projection.report;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface ReportTimeLogExcelView {

    UUID getTimeLogId();

    UUID getProjectId();

    UUID getTaskId();

    String getTaskTitle();

    UUID getSprintId();

    UUID getBacklogItemId();

    UUID getUserId();

    String getUsername();

    String getEmail();

    LocalDate getWorkDate();

    Integer getMinutes();

    String getDescription();

    Instant getCreatedAt();
}
