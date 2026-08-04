package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

public final class TaskWorkflowValidator {

    private TaskWorkflowValidator() {
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

        TaskStatusTransitionValidator.validate(oldStatus, newStatus);
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
        return TaskStatusTransitionValidator.isTerminal(status);
    }
}
