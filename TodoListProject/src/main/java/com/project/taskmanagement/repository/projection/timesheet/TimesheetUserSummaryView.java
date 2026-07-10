package com.project.taskmanagement.repository.projection.timesheet;

import java.util.UUID;

public interface TimesheetUserSummaryView {

    UUID getUserId();

    String getUsername();

    String getEmail();

    Long getTotalMinutes();

    Long getLogCount();

    Long getTaskCount();
}
