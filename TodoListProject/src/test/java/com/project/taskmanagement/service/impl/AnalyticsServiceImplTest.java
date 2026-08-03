package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock SprintRepository sprintRepository;
    @Mock BacklogItemRepository backlogItemRepository;
    @Mock TaskRepository taskRepository;
    @Mock TaskTimeLogRepository taskTimeLogRepository;
    @Mock ProjectActivityLogRepository projectActivityLogRepository;
    @Mock CurrentUserService currentUserService;
    @Mock ProjectAccessService projectAccessService;
    @Mock ObjectMapper objectMapper;

    @InjectMocks
    AnalyticsServiceImpl analyticsService;

    @Test
    void velocityCalculatesPerSprintRateAndRoundedAverages() {
        Project project = TestDataFactory.activeProject();
        User user = TestDataFactory.employee();
        Sprint first = TestDataFactory.sprint(project, SprintStatus.COMPLETED);
        Sprint second = TestDataFactory.sprint(project, SprintStatus.ACTIVE);
        first.setName("Sprint 1");
        second.setName("Sprint 2");

        when(currentUserService.getActiveCurrentUser()).thenReturn(user);
        when(projectAccessService.getProjectOrThrow(project.getId())).thenReturn(project);
        when(sprintRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId()))
                .thenReturn(List.of(second, first));

        when(backlogItemRepository.countByProjectIdAndSprintId(
                project.getId(), first.getId()
        )).thenReturn(4L);
        when(backlogItemRepository.countByProjectIdAndSprintIdAndStatus(
                project.getId(), first.getId(), BacklogItemStatus.DONE
        )).thenReturn(3L);
        when(backlogItemRepository.sumStoryPointsBySprint(
                project.getId(), first.getId()
        )).thenReturn(13L);
        when(backlogItemRepository.sumStoryPointsBySprintAndStatus(
                project.getId(), first.getId(), BacklogItemStatus.DONE
        )).thenReturn(8L);

        when(backlogItemRepository.countByProjectIdAndSprintId(
                project.getId(), second.getId()
        )).thenReturn(2L);
        when(backlogItemRepository.countByProjectIdAndSprintIdAndStatus(
                project.getId(), second.getId(), BacklogItemStatus.DONE
        )).thenReturn(1L);
        when(backlogItemRepository.sumStoryPointsBySprint(
                project.getId(), second.getId()
        )).thenReturn(5L);
        when(backlogItemRepository.sumStoryPointsBySprintAndStatus(
                project.getId(), second.getId(), BacklogItemStatus.DONE
        )).thenReturn(3L);

        VelocityChartResponse response = analyticsService.getVelocity(project.getId());

        assertThat(response.averageCompletedItems()).isEqualTo(2L);
        assertThat(response.averageCompletedStoryPoints()).isEqualTo(6L);
        assertThat(response.points()).hasSize(2);
        assertThat(response.points().get(0).completionRate()).isEqualTo(75.0);
        assertThat(response.points().get(1).completionRate()).isEqualTo(50.0);
    }
}
