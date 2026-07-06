package com.project.taskmanagement.repository.projection.report;

import java.time.LocalDate;
import java.util.UUID;

public interface ProjectTimeMemberView {

    UUID getUserId();

    Long getSpentMinutes();

    Long getLogCount();

    Long getTaskCount();

    LocalDate getFirstWorkDate();

    LocalDate getLastWorkDate();
}