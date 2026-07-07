package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.sprint.SprintCapacityResponse;
import com.project.taskmanagement.dto.response.sprint.SprintHealthResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRiskResponse;

import java.util.List;
import java.util.UUID;

public interface SprintInsightService {

    SprintCapacityResponse getCapacity(
            UUID projectId,
            UUID sprintId
    );

    SprintHealthResponse getHealth(
            UUID projectId,
            UUID sprintId
    );

    List<SprintRiskResponse> getRisks(
            UUID projectId,
            UUID sprintId
    );
}
