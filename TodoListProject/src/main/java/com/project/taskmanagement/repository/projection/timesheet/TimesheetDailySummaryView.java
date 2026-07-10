package com.project.taskmanagement.repository.projection.timesheet;

import java.time.LocalDate;

public interface TimesheetDailySummaryView {

    LocalDate getWorkDate();

    Long getTotalMinutes();

    Long getLogCount();
}
