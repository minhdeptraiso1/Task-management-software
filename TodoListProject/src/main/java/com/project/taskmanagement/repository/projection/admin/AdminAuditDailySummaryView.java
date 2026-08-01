package com.project.taskmanagement.repository.projection.admin;

import java.time.LocalDate;

public interface AdminAuditDailySummaryView {

    LocalDate getDate();

    Long getCount();
}
