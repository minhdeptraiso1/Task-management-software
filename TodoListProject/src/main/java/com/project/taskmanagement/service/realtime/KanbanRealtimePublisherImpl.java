package com.project.taskmanagement.service.realtime;

import com.project.taskmanagement.dto.realtime.KanbanRealtimeEvent;
import com.project.taskmanagement.dto.realtime.KanbanRealtimeEventType;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class KanbanRealtimePublisherImpl
        implements KanbanRealtimePublisher {

    SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishTaskCreated(
            Task task,
            UUID actorUserId,
            String actorUsername
    ) {
        publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_CREATED,
                actorUserId,
                actorUsername
        );
    }

    @Override
    public void publishTaskUpdated(
            Task task,
            UUID actorUserId,
            String actorUsername
    ) {
        publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_UPDATED,
                actorUserId,
                actorUsername
        );
    }

    @Override
    public void publishTaskStatusChanged(
            Task task,
            TaskStatus oldStatus,
            TaskStatus newStatus,
            Long oldPosition,
            Long newPosition,
            UUID actorUserId,
            String actorUsername
    ) {
        if (task == null
                || task.getCurrentSprintId() == null) {
            return;
        }

        publish(new KanbanRealtimeEvent(
                KanbanRealtimeEventType.TASK_STATUS_CHANGED,
                task.getProjectId(),
                task.getCurrentSprintId(),
                task.getId(),
                task.getBacklogItemId(),
                oldStatus,
                newStatus,
                oldPosition,
                newPosition,
                actorUserId,
                actorUsername,
                Instant.now()
        ));
    }

    @Override
    public void publishTaskPositionChanged(
            Task task,
            Long oldPosition,
            Long newPosition,
            UUID actorUserId,
            String actorUsername
    ) {
        if (task == null
                || task.getCurrentSprintId() == null) {
            return;
        }

        publish(new KanbanRealtimeEvent(
                KanbanRealtimeEventType.TASK_POSITION_CHANGED,
                task.getProjectId(),
                task.getCurrentSprintId(),
                task.getId(),
                task.getBacklogItemId(),
                task.getStatus(),
                task.getStatus(),
                oldPosition,
                newPosition,
                actorUserId,
                actorUsername,
                Instant.now()
        ));
    }

    @Override
    public void publishTaskAssigned(
            Task task,
            UUID actorUserId,
            String actorUsername
    ) {
        publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_ASSIGNED,
                actorUserId,
                actorUsername
        );
    }

    @Override
    public void publishTaskUnassigned(
            Task task,
            UUID actorUserId,
            String actorUsername
    ) {
        publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_UNASSIGNED,
                actorUserId,
                actorUsername
        );
    }

    @Override
    public void publishTaskDeleted(
            Task task,
            UUID actorUserId,
            String actorUsername
    ) {
        publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_DELETED,
                actorUserId,
                actorUsername
        );
    }

    @Override
    public void publishGeneric(
            Task task,
            KanbanRealtimeEventType eventType,
            UUID actorUserId,
            String actorUsername
    ) {
        if (task == null
                || task.getCurrentSprintId() == null
                || eventType == null) {
            return;
        }

        publish(new KanbanRealtimeEvent(
                eventType,
                task.getProjectId(),
                task.getCurrentSprintId(),
                task.getId(),
                task.getBacklogItemId(),
                task.getStatus(),
                task.getStatus(),
                task.getPosition(),
                task.getPosition(),
                actorUserId,
                actorUsername,
                Instant.now()
        ));
    }

    private void publish(
            KanbanRealtimeEvent event
    ) {
        if (event == null
                || event.projectId() == null
                || event.sprintId() == null) {
            return;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send(event);
                        }
                    }
            );
            return;
        }

        send(event);
    }

    private void send(KanbanRealtimeEvent event) {
        String destination =
                "/topic/projects/"
                        + event.projectId()
                        + "/sprints/"
                        + event.sprintId()
                        + "/kanban";

        try {
            messagingTemplate.convertAndSend(
                    destination,
                    event
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "Publish Kanban realtime event failed: destination={}, eventType={}, taskId={}",
                    destination,
                    event.eventType(),
                    event.taskId(),
                    exception
            );
        }
    }
}
