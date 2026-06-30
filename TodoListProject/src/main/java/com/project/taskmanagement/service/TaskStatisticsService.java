package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.taskstatistics.SprintBurndownResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;

import java.util.UUID;

public interface TaskStatisticsService {

    SprintTaskStatisticsResponse getSprintStatistics(
            UUID projectId,
            UUID sprintId
    );

    SprintBurndownResponse getSprintBurndown(
            UUID projectId,
            UUID sprintId
    );
}