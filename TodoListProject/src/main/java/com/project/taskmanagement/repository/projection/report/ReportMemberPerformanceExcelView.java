package com.project.taskmanagement.repository.projection.report;

import java.util.UUID;

public interface ReportMemberPerformanceExcelView {

    UUID getUserId();

    String getUsername();

    String getEmail();

    Long getTotalTasks();

    Long getDoneTasks();

    Long getActiveTasks();

    Long getBlockedTasks();

    Long getOverdueTasks();

    Long getEstimatedMinutes();

    Long getSpentMinutes();
}
