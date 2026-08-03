package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskStatusTransitionValidatorTest {

    @Test
    void acceptsSupportedWorkflowTransitions() {
        assertThatCode(() -> TaskStatusTransitionValidator.validate(
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS
        )).doesNotThrowAnyException();

        assertThatCode(() -> TaskStatusTransitionValidator.validate(
                TaskStatus.IN_REVIEW,
                TaskStatus.DONE
        )).doesNotThrowAnyException();

        assertThatCode(() -> TaskStatusTransitionValidator.validate(
                TaskStatus.DONE,
                TaskStatus.IN_PROGRESS
        )).doesNotThrowAnyException();
    }

    @Test
    void acceptsNoOpTransition() {
        assertThatCode(() -> TaskStatusTransitionValidator.validate(
                TaskStatus.IN_PROGRESS,
                TaskStatus.IN_PROGRESS
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnsupportedAndCancelledTransitions() {
        assertErrorCode(
                () -> TaskStatusTransitionValidator.validate(
                        TaskStatus.TODO,
                        TaskStatus.DONE
                ),
                ErrorCode.TASK_STATUS_TRANSITION_INVALID
        );

        assertErrorCode(
                () -> TaskStatusTransitionValidator.validate(
                        TaskStatus.CANCELLED,
                        TaskStatus.TODO
                ),
                ErrorCode.TASK_STATUS_TRANSITION_INVALID
        );
    }

    @Test
    void rejectsNullStatus() {
        assertErrorCode(
                () -> TaskStatusTransitionValidator.validate(
                        null,
                        TaskStatus.TODO
                ),
                ErrorCode.TASK_STATUS_INVALID
        );
    }

    @Test
    void classifiesTerminalAndActiveStatuses() {
        assertThat(TaskStatusTransitionValidator.isTerminal(TaskStatus.DONE)).isTrue();
        assertThat(TaskStatusTransitionValidator.isTerminal(TaskStatus.CANCELLED)).isTrue();
        assertThat(TaskStatusTransitionValidator.isActive(TaskStatus.IN_REVIEW)).isTrue();
        assertThat(TaskStatusTransitionValidator.isActive(TaskStatus.DONE)).isFalse();
    }

    private void assertErrorCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
            ErrorCode expected
    ) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(
                        ((BusinessException) error).getErrorCode()
                ).isEqualTo(expected));
    }
}
