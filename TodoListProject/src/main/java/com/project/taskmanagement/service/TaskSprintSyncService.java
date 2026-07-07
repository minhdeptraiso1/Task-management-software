package com.project.taskmanagement.service;

import java.util.UUID;

public interface TaskSprintSyncService {

    void attachBacklogItemTasksToSprint(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            UUID actorUserId
    );

    void detachBacklogItemTasksFromSprint(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            UUID actorUserId
    );

    void handleSprintCompletion(
            UUID projectId,
            UUID sprintId,
            UUID actorUserId
    );

    void handleSprintCancellation(
            UUID projectId,
            UUID sprintId,
            UUID actorUserId
    );
}
