package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskSprintSyncService;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskSprintSyncServiceImpl
        implements TaskSprintSyncService {

    TaskRepository taskRepository;
    ProjectActivityService projectActivityService;

    private static final List<TaskStatus>
            TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    @Override
    @Transactional
    public void attachBacklogItemTasksToSprint(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            UUID actorUserId
    ) {
        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndBacklogItemIdAndStatusNotInOrderByPositionAsc(
                                projectId,
                                backlogItemId,
                                TERMINAL_STATUSES
                        );

        if (tasks.isEmpty()) {
            return;
        }

        Long maxPosition =
                taskRepository.findMaxSprintPosition(
                        projectId,
                        sprintId,
                        TaskStatus.TODO
                );

        long nextPosition =
                maxPosition == null
                        ? 1L
                        : maxPosition + 1L;

        List<Task> changedTasks =
                new ArrayList<>();

        for (Task task : tasks) {
            UUID oldSprintId =
                    task.getCurrentSprintId();

            TaskStatus oldStatus =
                    task.getStatus();

            Long oldPosition =
                    task.getPosition();

            task.setCurrentSprintId(
                    sprintId
            );

            if (task.getOriginSprintId() == null) {
                task.setOriginSprintId(
                        sprintId
                );
            }

            /*
             * Task được đưa vào Sprint mới bắt đầu từ TODO.
             */
            task.setStatus(
                    TaskStatus.TODO
            );

            task.setCompletedAt(null);

            task.setPosition(
                    nextPosition++
            );

            changedTasks.add(task);

            logMove(
                    projectId,
                    task,
                    actorUserId,
                    ProjectActivityAction
                            .TASK_MOVED_TO_SPRINT,
                    oldSprintId,
                    sprintId,
                    oldStatus,
                    task.getStatus(),
                    oldPosition,
                    task.getPosition()
            );
        }

        taskRepository.saveAll(
                changedTasks
        );

        taskRepository.flush();
    }

    @Override
    @Transactional
    public void detachBacklogItemTasksFromSprint(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            UUID actorUserId
    ) {
        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndBacklogItemIdAndStatusNotInOrderByPositionAsc(
                                projectId,
                                backlogItemId,
                                TERMINAL_STATUSES
                        );

        if (tasks.isEmpty()) {
            return;
        }

        Long maxBacklogPosition =
                taskRepository
                        .findMaxBacklogPosition(
                                projectId,
                                TaskStatus.TODO
                        );

        long nextPosition =
                maxBacklogPosition == null
                        ? 1L
                        : maxBacklogPosition + 1L;

        List<Task> changedTasks =
                new ArrayList<>();

        for (Task task : tasks) {
            /*
             * Chỉ tách Task đang thực sự thuộc Sprint này.
             */
            if (!sprintId.equals(
                    task.getCurrentSprintId()
            )) {
                continue;
            }

            UUID oldSprintId =
                    task.getCurrentSprintId();

            TaskStatus oldStatus =
                    task.getStatus();

            Long oldPosition =
                    task.getPosition();

            task.setCurrentSprintId(null);
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
            task.setPosition(nextPosition++);

            changedTasks.add(task);

            logMove(
                    projectId,
                    task,
                    actorUserId,
                    ProjectActivityAction
                            .TASK_REMOVED_FROM_SPRINT,
                    oldSprintId,
                    null,
                    oldStatus,
                    TaskStatus.TODO,
                    oldPosition,
                    task.getPosition()
            );
        }

        if (!changedTasks.isEmpty()) {
            taskRepository.saveAll(
                    changedTasks
            );

            taskRepository.flush();
        }
        
        compactSprintPositions(
                projectId,
                sprintId
        );
    }

    @Override
    @Transactional
    public void handleSprintCancellation(
            UUID projectId,
            UUID sprintId,
            UUID actorUserId
    ) {
        List<Task> unfinishedTasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintIdAndStatusNotInOrderByPositionAsc(
                                projectId,
                                sprintId,
                                TERMINAL_STATUSES
                        );

        if (unfinishedTasks.isEmpty()) {
            return;
        }

        Long maxBacklogPosition =
                taskRepository
                        .findMaxBacklogPosition(
                                projectId,
                                TaskStatus.TODO
                        );

        long nextPosition =
                maxBacklogPosition == null
                        ? 1L
                        : maxBacklogPosition + 1L;

        for (Task task : unfinishedTasks) {
            TaskStatus oldStatus =
                    task.getStatus();

            Long oldPosition =
                    task.getPosition();

            task.setCurrentSprintId(null);
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
            task.setPosition(nextPosition++);

            logMove(
                    projectId,
                    task,
                    actorUserId,
                    ProjectActivityAction
                            .TASK_REMOVED_FROM_SPRINT,
                    sprintId,
                    null,
                    oldStatus,
                    TaskStatus.TODO,
                    oldPosition,
                    task.getPosition()
            );
        }

        taskRepository.saveAll(
                unfinishedTasks
        );

        taskRepository.flush();
    }

    private void logMove(
            UUID projectId,
            Task task,
            UUID actorUserId,
            ProjectActivityAction action,
            UUID oldSprintId,
            UUID newSprintId,
            TaskStatus oldStatus,
            TaskStatus newStatus,
            Long oldPosition,
            Long newPosition
    ) {
        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "currentSprintId",
                oldSprintId
        );

        oldValue.put(
                "status",
                oldStatus
        );

        oldValue.put(
                "position",
                oldPosition
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "currentSprintId",
                newSprintId
        );

        newValue.put(
                "status",
                newStatus
        );

        newValue.put(
                "position",
                newPosition
        );

        newValue.put(
                "originSprintId",
                task.getOriginSprintId()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        task.getId(),
                        action,
                        actorUserId,
                        oldValue,
                        newValue
                )
        );
    }

    private void compactSprintPositions(
            UUID projectId,
            UUID sprintId
    ) {
        if (sprintId == null) {
            return;
        }

        for (TaskStatus status :
                TaskStatus.values()) {

            List<Task> tasks =
                    taskRepository
                            .findAllByProjectIdAndCurrentSprintIdAndStatusOrderByPositionAsc(
                                    projectId,
                                    sprintId,
                                    status
                            );

            long expectedPosition = 1L;

            for (Task task : tasks) {
                if (!Long.valueOf(expectedPosition)
                        .equals(task.getPosition())) {

                    task.setPosition(
                            expectedPosition
                    );
                }

                expectedPosition++;
            }

            if (!tasks.isEmpty()) {
                taskRepository.saveAll(tasks);
            }
        }
    }
}