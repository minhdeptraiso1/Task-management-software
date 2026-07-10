package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TaskWorkflowValidator {

    private TaskWorkflowValidator() {
    }

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(TaskStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
                TaskStatus.TODO,
                EnumSet.of(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.BLOCKED,
                        TaskStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                TaskStatus.IN_PROGRESS,
                EnumSet.of(
                        TaskStatus.IN_REVIEW,
                        TaskStatus.BLOCKED,
                        TaskStatus.TODO,
                        TaskStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                TaskStatus.IN_REVIEW,
                EnumSet.of(
                        TaskStatus.DONE,
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.BLOCKED,
                        TaskStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                TaskStatus.BLOCKED,
                EnumSet.of(
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.CANCELLED
                )
        );

        ALLOWED_TRANSITIONS.put(
                TaskStatus.DONE,
                EnumSet.of(TaskStatus.IN_PROGRESS)
        );

        ALLOWED_TRANSITIONS.put(
                TaskStatus.CANCELLED,
                EnumSet.noneOf(TaskStatus.class)
        );
    }

    public static void validateTransition(
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        if (oldStatus == null || newStatus == null) {
            throw new BusinessException(
                    ErrorCode.TASK_STATUS_TRANSITION_INVALID
            );
        }

        if (oldStatus == newStatus) {
            return;
        }

        Set<TaskStatus> allowedStatuses =
                ALLOWED_TRANSITIONS.getOrDefault(
                        oldStatus,
                        EnumSet.noneOf(TaskStatus.class)
                );

        if (!allowedStatuses.contains(newStatus)) {
            throw new BusinessException(
                    ErrorCode.TASK_STATUS_TRANSITION_INVALID
            );
        }
    }

    public static void validateNotCancelled(
            TaskStatus status
    ) {
        if (status == TaskStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.TASK_CANCELLED_CANNOT_BE_UPDATED
            );
        }
    }

    public static void validateReopenTarget(
            TaskStatus targetStatus
    ) {
        if (targetStatus != TaskStatus.TODO
                && targetStatus != TaskStatus.IN_PROGRESS) {

            throw new BusinessException(
                    ErrorCode.TASK_DONE_REOPEN_TARGET_INVALID
            );
        }
    }

    public static boolean isTerminal(
            TaskStatus status
    ) {
        return status == TaskStatus.DONE
                || status == TaskStatus.CANCELLED;
    }
}
