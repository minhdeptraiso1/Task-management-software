package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateRangeValidator {

    private DateRangeValidator() {
    }

    public static void validate(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.FILTER_DATE_RANGE_INVALID);
        }
    }

    public static void validate(LocalDate from, LocalDate to, ErrorCode errorCode) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(errorCode);
        }
    }

    public static void validateMaxDays(LocalDate from, LocalDate to, long maxDays, ErrorCode errorCode) {
        validate(from, to, errorCode);

        if (from == null || to == null) {
            return;
        }

        if (ChronoUnit.DAYS.between(from, to) > maxDays) {
            throw new BusinessException(errorCode);
        }
    }

    public static void validate(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.FILTER_DATE_RANGE_INVALID);
        }
    }

    public static void validate(Instant from, Instant to, ErrorCode errorCode) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(errorCode);
        }
    }
}
