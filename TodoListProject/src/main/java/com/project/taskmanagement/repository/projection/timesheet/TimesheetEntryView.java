package com.project.taskmanagement.repository.projection.timesheet;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface TimesheetEntryView {

    UUID getId();

    UUID getProjectId();

    String getProjectCode();

    String getProjectName();

    UUID getTaskId();

    String getTaskTitle();

    UUID getUserId();

    String getUsername();

    String getEmail();

    LocalDate getWorkDate();

    Integer getMinutes();

    String getDescription();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
