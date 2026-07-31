package com.project.taskmanagement.dto.realtime;

import com.project.taskmanagement.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record KanbanRealtimeEvent(

        KanbanRealtimeEventType eventType,

        UUID projectId,

        UUID sprintId,

        UUID taskId,

        UUID backlogItemId,

        TaskStatus oldStatus,

        TaskStatus newStatus,

        Long oldPosition,

        Long newPosition,

        UUID actorUserId,

        String actorUsername,

        Instant occurredAt
) {
}
