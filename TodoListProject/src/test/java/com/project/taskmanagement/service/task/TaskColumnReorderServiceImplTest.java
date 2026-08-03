package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskColumnReorderServiceImplTest {

    private TaskColumnReorderService service;

    @BeforeEach
    void setUp() {
        service = new TaskColumnReorderServiceImpl(
                new TaskPositionHelper()
        );
    }

    @Test
    void movesTaskToBeginningAndKeepsContinuousUniquePositions() {
        Task first = task(1L, 1);
        Task second = task(2L, 2);
        Task third = task(3L, 3);
        List<Task> tasks = new ArrayList<>(List.of(first, second, third));

        service.reorderColumn(tasks, third.getId(), 1L);

        assertThat(third.getPosition()).isEqualTo(1L);
        assertThat(first.getPosition()).isEqualTo(2L);
        assertThat(second.getPosition()).isEqualTo(3L);
        assertContinuous(tasks);
    }

    @Test
    void movesTaskToEndAndClampsPositionsOutsideColumn() {
        Task first = task(1L, 1);
        Task second = task(2L, 2);
        Task third = task(3L, 3);
        List<Task> tasks = new ArrayList<>(List.of(first, second, third));

        service.reorderColumn(tasks, first.getId(), 99L);

        assertThat(first.getPosition()).isEqualTo(3L);
        assertContinuous(tasks);

        service.reorderColumn(tasks, first.getId(), 0L);
        assertThat(first.getPosition()).isEqualTo(1L);
        assertContinuous(tasks);
    }

    @Test
    void normalizesDuplicateAndNullPositionsDeterministically() {
        Task first = task(2L, 1);
        Task second = task(2L, 2);
        Task third = task(null, 3);
        List<Task> tasks = new ArrayList<>(List.of(third, second, first));

        service.normalizeColumn(tasks);

        assertThat(first.getPosition()).isEqualTo(1L);
        assertThat(second.getPosition()).isEqualTo(2L);
        assertThat(third.getPosition()).isEqualTo(3L);
        assertContinuous(tasks);
    }

    private Task task(Long position, int createdOrder) {
        Task task = Task.builder()
                .position(position)
                .build();
        task.setCreatedAt(Instant.parse(
                "2026-08-03T00:00:0" + createdOrder + "Z"
        ));
        return task;
    }

    private void assertContinuous(List<Task> tasks) {
        assertThat(tasks)
                .extracting(Task::getPosition)
                .containsExactlyInAnyOrder(1L, 2L, 3L)
                .doesNotHaveDuplicates();
    }
}
