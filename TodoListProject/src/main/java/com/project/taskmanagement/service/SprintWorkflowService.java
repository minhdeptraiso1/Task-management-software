package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.sprint.SprintResponse;

import java.util.UUID;

public interface SprintWorkflowService {

    SprintResponse start(
            UUID projectId,
            UUID sprintId
    );

    SprintResponse complete(
            UUID projectId,
            UUID sprintId
    );

    SprintResponse cancel(
            UUID projectId,
            UUID sprintId
    );
}
