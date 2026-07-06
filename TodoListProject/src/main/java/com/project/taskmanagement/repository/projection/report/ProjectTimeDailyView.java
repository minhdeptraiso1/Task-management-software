package com.project.taskmanagement.repository.projection.report;

import java.time.LocalDate;

public interface ProjectTimeDailyView {

    LocalDate getWorkDate();

    Long getSpentMinutes();

    Long getLogCount();
}