package com.project.taskmanagement.repository.projection.report;

import java.util.UUID;

public interface ProjectTimeTaskView {

    UUID getTaskId();

    Long getSpentMinutes();

    Long getLogCount();

    Long getContributorCount();
}