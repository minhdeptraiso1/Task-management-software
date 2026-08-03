package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskKanbanLockServiceImplTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    SprintRepository sprintRepository;

    TaskKanbanLockService service;

    @BeforeEach
    void setUp() {
        service = new TaskKanbanLockServiceImpl(
                taskRepository,
                sprintRepository
        );
    }

    @Test
    void locksSprintParentThenRequestedSprintColumn() {
        UUID projectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();
        Sprint sprint = new Sprint();
        List<Task> expected = List.of(new Task());

        when(sprintRepository.findWithLockByIdAndProjectId(sprintId, projectId))
                .thenReturn(Optional.of(sprint));
        when(taskRepository.lockSprintColumnTasks(
                projectId, sprintId, TaskStatus.TODO
        )).thenReturn(expected);

        assertThat(service.lockColumn(projectId, sprintId, TaskStatus.TODO))
                .isSameAs(expected);

        var inOrder = inOrder(sprintRepository, taskRepository);
        inOrder.verify(sprintRepository)
                .findWithLockByIdAndProjectId(sprintId, projectId);
        inOrder.verify(taskRepository)
                .lockSprintColumnTasks(projectId, sprintId, TaskStatus.TODO);
    }

    @Test
    void usesIsNullQueryForBacklogColumn() {
        UUID projectId = UUID.randomUUID();
        List<Task> expected = List.of(new Task());
        when(taskRepository.lockBacklogColumnTasks(projectId, TaskStatus.TODO))
                .thenReturn(expected);

        assertThat(service.lockColumn(projectId, null, TaskStatus.TODO))
                .isSameAs(expected);

        verifyNoInteractions(sprintRepository);
        verify(taskRepository)
                .lockBacklogColumnTasks(projectId, TaskStatus.TODO);
    }
}
