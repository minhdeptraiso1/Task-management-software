package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public final class TaskStatusTransitionValidator {

    private static final EnumMap<TaskStatus, Set<TaskStatus>>
            ALLOWED_TRANSITIONS =
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

    private TaskStatusTransitionValidator() {
    }

    public static void validate(
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        if (oldStatus == null
                || newStatus == null) {

            throw new BusinessException(
                    ErrorCode.TASK_STATUS_INVALID
            );
        }

        if (oldStatus == newStatus) {
            return;
        }

        Set<TaskStatus> allowed =
                ALLOWED_TRANSITIONS
                        .getOrDefault(
                                oldStatus,
                                Set.of()
                        );

        if (!allowed.contains(newStatus)) {
            throw new BusinessException(
                    ErrorCode.TASK_STATUS_TRANSITION_INVALID
            );
        }
    }

    public static boolean isTerminal(
            TaskStatus status
    ) {
        return status == TaskStatus.DONE
                || status == TaskStatus.CANCELLED;
    }

    public static boolean isActive(
            TaskStatus status
    ) {
        return status != TaskStatus.DONE
                && status != TaskStatus.CANCELLED;
    }
}
