package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.time.LocalDate;

public final class SprintValidator {

    private SprintValidator() {
    }

    public static void validateProjectEditable(
            Project project
    ) {
        if (project.getStatus() == ProjectStatus.ARCHIVED
                || project.getStatus() == ProjectStatus.COMPLETED
                || project.getStatus() == ProjectStatus.CANCELLED) {

            throw new BusinessException(
                    ErrorCode.PROJECT_NOT_EDITABLE
            );
        }
    }

    public static void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new BusinessException(
                    ErrorCode.SPRINT_DATE_INVALID
            );
        }
    }

    public static void validateEditable(
            Sprint sprint
    ) {
        if (sprint.getStatus()
                != SprintStatus.PLANNING) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_EDITABLE
            );
        }
    }

    public static void validatePlanning(
            Sprint sprint
    ) {
        if (sprint.getStatus()
                != SprintStatus.PLANNING) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_PLANNING
            );
        }
    }

    public static void validateCanStart(
            Sprint sprint
    ) {
        if (sprint.getStatus()
                != SprintStatus.PLANNING) {

            throw new BusinessException(
                    ErrorCode.SPRINT_START_INVALID
            );
        }
    }

    public static void validateCanComplete(
            Sprint sprint
    ) {
        if (sprint.getStatus()
                != SprintStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.SPRINT_COMPLETE_INVALID
            );
        }
    }

    public static void validateCanCancel(
            Sprint sprint
    ) {
        boolean canCancel =
                sprint.getStatus() == SprintStatus.PLANNING
                        || sprint.getStatus()
                        == SprintStatus.ACTIVE;

        if (!canCancel) {
            throw new BusinessException(
                    ErrorCode.SPRINT_CANCEL_INVALID
            );
        }
    }
}