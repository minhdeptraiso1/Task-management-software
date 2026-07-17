package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public final class BugStatusTransitionValidator {

    private static final EnumMap<BugStatus, Set<BugStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(BugStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(BugStatus.OPEN,
                EnumSet.of(BugStatus.ASSIGNED, BugStatus.IN_PROGRESS, BugStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(BugStatus.ASSIGNED,
                EnumSet.of(BugStatus.IN_PROGRESS, BugStatus.OPEN, BugStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(BugStatus.IN_PROGRESS,
                EnumSet.of(BugStatus.RESOLVED, BugStatus.OPEN, BugStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(BugStatus.RESOLVED,
                EnumSet.of(BugStatus.VERIFIED, BugStatus.REOPENED, BugStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(BugStatus.VERIFIED,
                EnumSet.of(BugStatus.CLOSED, BugStatus.REOPENED));
        ALLOWED_TRANSITIONS.put(BugStatus.REOPENED,
                EnumSet.of(BugStatus.ASSIGNED, BugStatus.IN_PROGRESS, BugStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(BugStatus.CLOSED, EnumSet.noneOf(BugStatus.class));
        ALLOWED_TRANSITIONS.put(BugStatus.CANCELLED, EnumSet.noneOf(BugStatus.class));
    }

    private BugStatusTransitionValidator() {
    }

    public static void validate(BugStatus oldStatus, BugStatus newStatus) {
        if (oldStatus == null || newStatus == null) {
            throw new BusinessException(ErrorCode.BUG_STATUS_INVALID);
        }
        if (oldStatus == newStatus) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of()).contains(newStatus)) {
            throw new BusinessException(ErrorCode.BUG_STATUS_TRANSITION_INVALID);
        }
    }

    public static boolean isClosed(BugStatus status) {
        return status == BugStatus.CLOSED || status == BugStatus.CANCELLED;
    }
}
