package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.time.LocalDate;

public final class ProjectValidator {

    private ProjectValidator() {
    }

    public static void validateCreator(User user) {
        if (user == null) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        if (!user.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        if (user.getRole() != UserRole.MANAGER) {
            throw new BusinessException(
                    ErrorCode.PROJECT_CREATE_FORBIDDEN
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
                    ErrorCode.PROJECT_DATE_INVALID
            );
        }
    }

    public static void validateEditable(
            Project project
    ) {
        if (project.getStatus()
                == ProjectStatus.ARCHIVED) {

            throw new BusinessException(
                    ErrorCode.PROJECT_NOT_EDITABLE
            );
        }
    }

    public static void validateStatusTransition(
            ProjectStatus currentStatus,
            ProjectStatus newStatus
    ) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case PLANNING -> newStatus == ProjectStatus.ACTIVE
                    || newStatus == ProjectStatus.CANCELLED;

            case ACTIVE -> newStatus == ProjectStatus.ON_HOLD
                    || newStatus == ProjectStatus.COMPLETED
                    || newStatus == ProjectStatus.CANCELLED;

            case ON_HOLD -> newStatus == ProjectStatus.ACTIVE
                    || newStatus == ProjectStatus.CANCELLED;

            case COMPLETED, CANCELLED -> newStatus == ProjectStatus.ARCHIVED;

            case ARCHIVED -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    ErrorCode.PROJECT_STATUS_TRANSITION_INVALID
            );
        }
    }
}