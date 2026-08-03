package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTimeLogValidatorTest {

    @Test
    void acceptsDailyTotalAtLimit() {
        TaskTimeLogValidator.validateDailyLimit(710L, 10);
    }

    @Test
    void rejectsDailyTotalOverTwelveHours() {
        assertThatThrownBy(() ->
                TaskTimeLogValidator.validateDailyLimit(710L, 11)
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(
                        ((BusinessException) exception).getErrorCode()
                ).isEqualTo(ErrorCode.TASK_TIME_LOG_DAILY_LIMIT_EXCEEDED));
    }

    @Test
    void rejectsSingleLogOverTwelveHours() {
        assertThatThrownBy(() ->
                TaskTimeLogValidator.validateMinutes(721)
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(
                        ((BusinessException) exception).getErrorCode()
                ).isEqualTo(ErrorCode.TASK_TIME_LOG_MINUTES_TOO_LARGE));
    }
}
