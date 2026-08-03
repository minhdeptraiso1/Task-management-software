package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.time.LocalDate;

public final class TaskTimeLogValidator {

    private static final int MAX_DAILY_MINUTES = 720;

    private TaskTimeLogValidator() {
    }

    public static void validateTask(
            Task task
    ) {
        if (task.getStatus()
                == TaskStatus.CANCELLED) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_TASK_CANCELLED
            );
        }
    }

    public static void validateWorkDate(
            LocalDate workDate
    ) {
        if (workDate == null) {
            throw new BusinessException(
                    ErrorCode.TASK_TIME_LOG_DATE_INVALID
            );
        }

        if (workDate.isAfter(
                LocalDate.now()
        )) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_FUTURE_DATE_INVALID
            );
        }
    }

    public static void validateMinutes(
            Integer minutes
    ) {
        if (minutes == null
                || minutes <= 0) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_MINUTES_INVALID
            );
        }

        if (minutes > MAX_DAILY_MINUTES) {
            throw new BusinessException(
                    ErrorCode.TASK_TIME_LOG_MINUTES_TOO_LARGE
            );
        }
    }

    public static void validateDailyLimit(
            long currentDailyMinutes,
            int newMinutes
    ) {
        if (currentDailyMinutes + newMinutes
                > MAX_DAILY_MINUTES) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_DAILY_LIMIT_EXCEEDED
            );
        }
    }

    public static String normalizeDescription(
            String description
    ) {
        if (description == null
                || description.trim().isBlank()) {
            return null;
        }

        String normalized =
                description.trim();

        if (normalized.length() > 2000) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR
            );
        }

        return normalized;
    }
}
