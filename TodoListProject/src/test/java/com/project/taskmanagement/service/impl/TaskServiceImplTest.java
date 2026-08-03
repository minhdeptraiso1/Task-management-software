package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.task.UpdateTaskStatusRequest;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.cache.CacheEvictService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.BacklogItemLookupHelper;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.realtime.KanbanRealtimePublisher;
import com.project.taskmanagement.service.task.TaskColumnReorderService;
import com.project.taskmanagement.service.task.TaskKanbanLockService;
import com.project.taskmanagement.service.task.TaskViewHelper;
import com.project.taskmanagement.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock TaskRepository taskRepository;
    @Mock TaskTimeLogRepository taskTimeLogRepository;
    @Mock BacklogItemRepository backlogItemRepository;
    @Mock UserRepository userRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock UserLookupHelper userLookupHelper;
    @Mock BacklogItemLookupHelper backlogItemLookupHelper;
    @Mock CurrentUserService currentUserService;
    @Mock ProjectAccessService projectAccessService;
    @Mock ProjectActivityService projectActivityService;
    @Mock NotificationService notificationService;
    @Mock TaskViewHelper taskViewHelper;
    @Mock TaskKanbanLockService taskKanbanLockService;
    @Mock TaskColumnReorderService taskColumnReorderService;
    @Mock KanbanRealtimePublisher kanbanRealtimePublisher;
    @Mock CacheEvictService cacheEvictService;
    @Mock SprintRepository sprintRepository;

    @InjectMocks
    TaskServiceImpl taskService;

    @Test
    void updateStatus_inReviewToDone_setsCompletedAtAndPublishesSideEffects() {
        WorkflowFixture fixture = fixture(TaskStatus.IN_REVIEW);
        stubColumns(fixture.task(), TaskStatus.IN_REVIEW, TaskStatus.DONE);
        stubResponseDependencies();

        TaskResponse response = taskService.updateStatus(
                fixture.project().getId(),
                fixture.task().getId(),
                new UpdateTaskStatusRequest(TaskStatus.DONE, 1L)
        );

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.completedAt()).isNotNull();
        assertThat(fixture.task().getCompletedAt()).isNotNull();
        verify(projectActivityService).log(any());
        verify(kanbanRealtimePublisher).publishTaskStatusChanged(
                fixture.task(),
                TaskStatus.IN_REVIEW,
                TaskStatus.DONE,
                1L,
                1L,
                fixture.user().getId(),
                fixture.user().getUsername()
        );
        verify(cacheEvictService).evictTaskWorkspace(
                fixture.project().getId(),
                fixture.sprint().getId(),
                fixture.task().getId(),
                fixture.task().getAssigneeUserId()
        );
    }

    @Test
    void updateStatus_doneToInProgress_clearsCompletedAt() {
        WorkflowFixture fixture = fixture(TaskStatus.DONE);
        fixture.task().setCompletedAt(java.time.Instant.now());
        stubColumns(fixture.task(), TaskStatus.DONE, TaskStatus.IN_PROGRESS);
        stubResponseDependencies();

        TaskResponse response = taskService.updateStatus(
                fixture.project().getId(),
                fixture.task().getId(),
                new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, 1L)
        );

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.completedAt()).isNull();
        assertThat(fixture.task().getCompletedAt()).isNull();
    }

    @Test
    void updateStatus_todoToDone_rejectsInvalidTransitionBeforeLockingColumns() {
        WorkflowFixture fixture = fixture(TaskStatus.TODO);

        assertThatThrownBy(() -> taskService.updateStatus(
                fixture.project().getId(),
                fixture.task().getId(),
                new UpdateTaskStatusRequest(TaskStatus.DONE, 1L)
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(
                        ((BusinessException) error).getErrorCode()
                ).isEqualTo(ErrorCode.TASK_STATUS_TRANSITION_INVALID));

        verify(taskKanbanLockService, never()).lockColumn(any(), any(), any());
        verify(projectActivityService, never()).log(any());
    }

    private WorkflowFixture fixture(TaskStatus status) {
        Project project = TestDataFactory.activeProject();
        User user = TestDataFactory.employee();
        Sprint sprint = TestDataFactory.sprint(project, SprintStatus.ACTIVE);
        Task task = TestDataFactory.task(project, sprint, status);
        task.setAssigneeUserId(user.getId());

        when(currentUserService.getActiveCurrentUser()).thenReturn(user);
        when(projectAccessService.getProjectOrThrow(project.getId())).thenReturn(project);
        when(taskRepository.findByIdAndProjectId(task.getId(), project.getId()))
                .thenReturn(Optional.of(task));
        when(sprintRepository.findByIdAndProjectId(sprint.getId(), project.getId()))
                .thenReturn(Optional.of(sprint));

        return new WorkflowFixture(project, sprint, task, user);
    }

    private void stubColumns(
            Task task,
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        when(taskKanbanLockService.lockColumn(
                task.getProjectId(),
                task.getCurrentSprintId(),
                oldStatus
        )).thenReturn(new ArrayList<>(List.of(task)));
        when(taskKanbanLockService.lockColumn(
                task.getProjectId(),
                task.getCurrentSprintId(),
                newStatus
        )).thenReturn(new ArrayList<>());
    }

    private void stubResponseDependencies() {
        when(userLookupHelper.findUserMap(anyList())).thenReturn(Map.of());
        when(taskTimeLogRepository.sumMinutesGroupedByTaskIds(anyList()))
                .thenReturn(List.of());
    }

    private record WorkflowFixture(
            Project project,
            Sprint sprint,
            Task task,
            User user
    ) {
    }
}
