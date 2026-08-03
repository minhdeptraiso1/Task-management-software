package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskValidatorTest {

    @Test
    void allowsEditableTaskAndActiveProject() {
        Task task = Task.builder().status(TaskStatus.TODO).build();
        Project project = Project.builder().status(ProjectStatus.ACTIVE).build();

        assertThatCode(() -> TaskValidator.validateEditable(task))
                .doesNotThrowAnyException();
        assertThatCode(() -> TaskValidator.validateProjectEditable(project))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNullCancelledAndDoneTaskForMainInfoUpdate() {
        assertErrorCode(
                () -> TaskValidator.validateEditable(null),
                ErrorCode.TASK_NOT_FOUND
        );
        assertErrorCode(
                () -> TaskValidator.validateEditable(
                        Task.builder().status(TaskStatus.CANCELLED).build()
                ),
                ErrorCode.TASK_CANCELLED_CANNOT_BE_UPDATED
        );
        assertErrorCode(
                () -> TaskValidator.validateMainInfoEditable(
                        Task.builder().status(TaskStatus.DONE).build()
                ),
                ErrorCode.TASK_DONE_CANNOT_BE_UPDATED_EXCEPT_REOPEN
        );
    }

    @Test
    void validatesDateRangeAndClosedProject() {
        LocalDate start = LocalDate.of(2026, 8, 3);

        assertThatCode(() -> TaskValidator.validateDates(start, start))
                .doesNotThrowAnyException();
        assertErrorCode(
                () -> TaskValidator.validateDates(start, start.minusDays(1)),
                ErrorCode.TASK_DATE_INVALID
        );
        assertErrorCode(
                () -> TaskValidator.validateProjectEditable(
                        Project.builder().status(ProjectStatus.ARCHIVED).build()
                ),
                ErrorCode.PROJECT_NOT_EDITABLE
        );
    }

    @Test
    void requiresTaskToBelongToActiveSprint() {
        Task task = Task.builder()
                .currentSprintId(java.util.UUID.randomUUID())
                .build();
        Sprint active = Sprint.builder().status(SprintStatus.ACTIVE).build();

        assertThatCode(() -> TaskValidator.validateTaskInActiveSprint(task, active))
                .doesNotThrowAnyException();
        assertErrorCode(
                () -> TaskValidator.validateTaskInActiveSprint(
                        task,
                        Sprint.builder().status(SprintStatus.PLANNING).build()
                ),
                ErrorCode.TASK_KANBAN_SPRINT_INVALID
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
