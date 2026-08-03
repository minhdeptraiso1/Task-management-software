package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SprintValidatorTest {

    @Test
    void validatesStartCompleteAndCancelStates() {
        assertThatCode(() -> SprintValidator.validateCanStart(
                Sprint.builder().status(SprintStatus.PLANNING).build()
        )).doesNotThrowAnyException();
        assertThatCode(() -> SprintValidator.validateCanComplete(
                Sprint.builder().status(SprintStatus.ACTIVE).build()
        )).doesNotThrowAnyException();
        assertThatCode(() -> SprintValidator.validateCanCancel(
                Sprint.builder().status(SprintStatus.ACTIVE).build()
        )).doesNotThrowAnyException();

        assertErrorCode(
                () -> SprintValidator.validateCanStart(
                        Sprint.builder().status(SprintStatus.ACTIVE).build()
                ),
                ErrorCode.SPRINT_CANNOT_START
        );
        assertErrorCode(
                () -> SprintValidator.validateCanComplete(
                        Sprint.builder().status(SprintStatus.PLANNING).build()
                ),
                ErrorCode.SPRINT_CANNOT_COMPLETE
        );
        assertErrorCode(
                () -> SprintValidator.validateCanCancel(
                        Sprint.builder().status(SprintStatus.COMPLETED).build()
                ),
                ErrorCode.SPRINT_CANCEL_INVALID
        );
    }

    @Test
    void validatesDatesEditableStateAndProjectState() {
        LocalDate start = LocalDate.of(2026, 8, 3);

        assertThatCode(() -> SprintValidator.validateDates(start, start.plusDays(7)))
                .doesNotThrowAnyException();
        assertErrorCode(
                () -> SprintValidator.validateDates(start, start.minusDays(1)),
                ErrorCode.SPRINT_DATE_INVALID
        );
        assertErrorCode(
                () -> SprintValidator.validateEditable(
                        Sprint.builder().status(SprintStatus.COMPLETED).build()
                ),
                ErrorCode.SPRINT_CANNOT_UPDATE_CLOSED
        );
        assertErrorCode(
                () -> SprintValidator.validateProjectEditable(
                        Project.builder().status(ProjectStatus.CANCELLED).build()
                ),
                ErrorCode.PROJECT_NOT_EDITABLE
        );
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
