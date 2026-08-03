package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.SprintMapper;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskSprintSyncService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.cache.CacheEvictService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintWorkflowServiceImplTest {

    @Mock SprintRepository sprintRepository;
    @Mock BacklogItemRepository backlogItemRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock TaskRepository taskRepository;
    @Mock SprintMapper sprintMapper;
    @Mock CurrentUserService currentUserService;
    @Mock ProjectAccessService projectAccessService;
    @Mock ProjectActivityService projectActivityService;
    @Mock NotificationService notificationService;
    @Mock TaskSprintSyncService taskSprintSyncService;
    @Mock CacheEvictService cacheEvictService;

    @InjectMocks
    SprintWorkflowServiceImpl sprintWorkflowService;

    @Test
    void start_planningSprintWithBacklog_activatesSprint() {
        SprintFixture fixture = fixture(SprintStatus.PLANNING);
        when(sprintRepository.existsByProjectIdAndStatus(
                fixture.project().getId(),
                SprintStatus.ACTIVE
        )).thenReturn(false);
        when(backlogItemRepository.countByProjectIdAndSprintId(
                fixture.project().getId(),
                fixture.sprint().getId()
        )).thenReturn(1L);
        when(sprintRepository.save(fixture.sprint())).thenReturn(fixture.sprint());
        when(projectMemberRepository.findAllByProjectIdOrderByJoinedAtAsc(
                fixture.project().getId()
        )).thenReturn(List.of());

        sprintWorkflowService.start(
                fixture.project().getId(),
                fixture.sprint().getId()
        );

        assertThat(fixture.sprint().getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(fixture.sprint().getStartedAt()).isNotNull();
        verify(projectActivityService).log(any());
        verify(cacheEvictService).evictSprintWorkspace(
                fixture.project().getId(),
                fixture.sprint().getId()
        );
    }

    @Test
    void start_emptySprint_isRejectedWithoutSaving() {
        SprintFixture fixture = fixture(SprintStatus.PLANNING);
        when(sprintRepository.existsByProjectIdAndStatus(
                fixture.project().getId(),
                SprintStatus.ACTIVE
        )).thenReturn(false);
        when(backlogItemRepository.countByProjectIdAndSprintId(
                fixture.project().getId(),
                fixture.sprint().getId()
        )).thenReturn(0L);

        assertErrorCode(
                () -> sprintWorkflowService.start(
                        fixture.project().getId(),
                        fixture.sprint().getId()
                ),
                ErrorCode.SPRINT_EMPTY_CANNOT_START
        );

        verify(sprintRepository, never()).save(any());
    }

    @Test
    void start_whenAnotherSprintIsActive_isRejected() {
        SprintFixture fixture = fixture(SprintStatus.PLANNING);
        when(sprintRepository.existsByProjectIdAndStatus(
                fixture.project().getId(),
                SprintStatus.ACTIVE
        )).thenReturn(true);

        assertErrorCode(
                () -> sprintWorkflowService.start(
                        fixture.project().getId(),
                        fixture.sprint().getId()
                ),
                ErrorCode.SPRINT_ALREADY_ACTIVE
        );
    }

    @Test
    void complete_requiresActiveSprint() {
        SprintFixture fixture = fixture(SprintStatus.PLANNING);

        assertErrorCode(
                () -> sprintWorkflowService.complete(
                        fixture.project().getId(),
                        fixture.sprint().getId()
                ),
                ErrorCode.SPRINT_NOT_ACTIVE
        );

        verify(taskSprintSyncService, never())
                .handleSprintCompletion(any(), any(), any());
    }

    private SprintFixture fixture(SprintStatus status) {
        Project project = TestDataFactory.activeProject();
        User user = TestDataFactory.employee();
        Sprint sprint = TestDataFactory.sprint(project, status);

        when(currentUserService.getActiveCurrentUser()).thenReturn(user);
        when(projectAccessService.getProjectOrThrow(project.getId())).thenReturn(project);
        when(sprintRepository.findByIdAndProjectId(sprint.getId(), project.getId()))
                .thenReturn(Optional.of(sprint));

        return new SprintFixture(project, sprint, user);
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

    private record SprintFixture(
            Project project,
            Sprint sprint,
            User user
    ) {
    }
}
