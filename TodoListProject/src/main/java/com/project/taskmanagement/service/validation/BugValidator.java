package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

public final class BugValidator {

    private BugValidator() {
    }

    public static void validateEditable(Bug bug) {
        if (bug == null) {
            throw new BusinessException(ErrorCode.BUG_NOT_FOUND);
        }
        if (BugStatusTransitionValidator.isClosed(bug.getStatus())) {
            throw new BusinessException(ErrorCode.BUG_CLOSED_CANNOT_BE_UPDATED);
        }
    }
}
