package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

public final class BacklogItemValidator {

    private BacklogItemValidator() {
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

    public static void validateEditable(
            BacklogItem backlogItem
    ) {
        if (backlogItem.getStatus()
                == BacklogItemStatus.DONE
                || backlogItem.getStatus()
                == BacklogItemStatus.CANCELLED) {

            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_NOT_EDITABLE
            );
        }
    }

    public static void validateNotInSprint(
            BacklogItem backlogItem
    ) {
        if (backlogItem.getSprintId() != null) {
            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_ALREADY_IN_SPRINT
            );
        }
    }

    public static void validateStatusTransition(
            BacklogItemStatus currentStatus,
            BacklogItemStatus newStatus
    ) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case DRAFT -> newStatus == BacklogItemStatus.READY
                    || newStatus
                    == BacklogItemStatus.CANCELLED;

            case READY -> newStatus == BacklogItemStatus.DRAFT
                    || newStatus
                    == BacklogItemStatus.IN_SPRINT
                    || newStatus
                    == BacklogItemStatus.CANCELLED;

            case IN_SPRINT -> newStatus == BacklogItemStatus.READY
                    || newStatus == BacklogItemStatus.DONE
                    || newStatus
                    == BacklogItemStatus.CANCELLED;

            case DONE -> false;

            case CANCELLED -> newStatus == BacklogItemStatus.DRAFT;
        };

        if (!valid) {
            throw new BusinessException(
                    ErrorCode
                            .BACKLOG_ITEM_STATUS_TRANSITION_INVALID
            );
        }
    }
}