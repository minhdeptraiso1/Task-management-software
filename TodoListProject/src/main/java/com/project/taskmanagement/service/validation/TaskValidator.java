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
        if (task == null) {
            throw new BusinessException(
                    ErrorCode.TASK_NOT_FOUND
            );
        }

        if (task.getStatus()
                == TaskStatus.CANCELLED) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_CANCELLED_CANNOT_BE_UPDATED
            );
        }
    }

    public static void validateMainInfoEditable(
            Task task
    ) {
        validateEditable(task);

        if (task.getStatus()
                == TaskStatus.DONE) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_DONE_CANNOT_BE_UPDATED_EXCEPT_REOPEN
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

    public static void validateTaskInActiveSprint(
            Task task,
            Sprint sprint
    ) {
        if (task.getCurrentSprintId() == null) {
            throw new BusinessException(
                    ErrorCode.TASK_KANBAN_SPRINT_INVALID
            );
        }

        if (sprint == null
                || sprint.getStatus() != SprintStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.TASK_KANBAN_SPRINT_INVALID
            );
        }
    }
}
