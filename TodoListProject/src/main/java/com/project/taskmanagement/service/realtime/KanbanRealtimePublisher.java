package com.project.taskmanagement.service.realtime;

import com.project.taskmanagement.dto.realtime.KanbanRealtimeEventType;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;

import java.util.UUID;

public interface KanbanRealtimePublisher {

    void publishTaskCreated(
            Task task,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskUpdated(
            Task task,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskStatusChanged(
            Task task,
            TaskStatus oldStatus,
            TaskStatus newStatus,
            Long oldPosition,
            Long newPosition,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskPositionChanged(
            Task task,
            Long oldPosition,
            Long newPosition,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskAssigned(
            Task task,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskUnassigned(
            Task task,
            UUID actorUserId,
            String actorUsername
    );

    void publishTaskDeleted(
            Task task,
            UUID actorUserId,
            String actorUsername
    );

    void publishGeneric(
            Task task,
            KanbanRealtimeEventType eventType,
            UUID actorUserId,
            String actorUsername
    );
}
