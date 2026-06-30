package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.time.LocalDate;

public final class TaskValidator {

    private TaskValidator() {
    }

    public static void validateProjectEditable(
            Project project
    ) {
        if (project.getStatus()
                == ProjectStatus.COMPLETED
                || project.getStatus()
                == ProjectStatus.CANCELLED
                || project.getStatus()
                == ProjectStatus.ARCHIVED) {

            throw new BusinessException(
                    ErrorCode.PROJECT_NOT_EDITABLE
            );
        }
    }

    public static void validateDates(
            LocalDate startDate,
            LocalDate dueDate
    ) {
        if (startDate != null
                && dueDate != null
                && dueDate.isBefore(startDate)) {

            throw new BusinessException(
                    ErrorCode.TASK_DATE_INVALID
            );
        }
    }

    public static void validateEditable(
            Task task
    ) {
        if (task.getStatus()
                == TaskStatus.CANCELLED) {

            throw new BusinessException(
                    ErrorCode.TASK_NOT_EDITABLE
            );
        }
    }

    public static void validateStatusTransition(
            TaskStatus currentStatus,
            TaskStatus newStatus
    ) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {

            case TODO -> newStatus == TaskStatus.IN_PROGRESS
                    || newStatus == TaskStatus.BLOCKED
                    || newStatus == TaskStatus.CANCELLED;

            case IN_PROGRESS -> newStatus == TaskStatus.TODO
                    || newStatus == TaskStatus.IN_REVIEW
                    || newStatus == TaskStatus.BLOCKED
                    || newStatus == TaskStatus.CANCELLED;

            case IN_REVIEW -> newStatus == TaskStatus.IN_PROGRESS
                    || newStatus == TaskStatus.DONE
                    || newStatus == TaskStatus.BLOCKED
                    || newStatus == TaskStatus.CANCELLED;

            case BLOCKED -> newStatus == TaskStatus.TODO
                    || newStatus == TaskStatus.IN_PROGRESS
                    || newStatus == TaskStatus.CANCELLED;

            case DONE -> newStatus == TaskStatus.IN_PROGRESS;

            case CANCELLED -> newStatus == TaskStatus.TODO;
        };

        if (!valid) {
            throw new BusinessException(
                    ErrorCode.TASK_STATUS_TRANSITION_INVALID
            );
        }
    }

    public static void validateSprintActive(
            Sprint sprint
    ) {
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.TASK_NOT_IN_ACTIVE_SPRINT
            );
        }
    }
}