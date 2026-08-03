package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.spec.ProjectActivitySpecification;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectActivityRepositoryTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectActivityLogRepository projectActivityLogRepository;

    @Test
    void filtersActivityByProjectActionActorAndKeyword() {
        User actor = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-activity",
                "repo-activity@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-ACTIVITY",
                "Activity project",
                actor.getId()
        ));
        UUID expectedTaskId = UUID.randomUUID();
        ProjectActivityLog expected = TestEntityFactory.activity(
                project.getId(),
                expectedTaskId,
                actor.getId(),
                ProjectActivityAction.TASK_STATUS_CHANGED,
                "{\"status\":\"DONE\",\"title\":\"OAuth login\"}"
        );
        ProjectActivityLog wrongAction = TestEntityFactory.activity(
                project.getId(),
                UUID.randomUUID(),
                actor.getId(),
                ProjectActivityAction.TASK_CREATED,
                "{\"title\":\"OAuth login\"}"
        );
        projectActivityLogRepository.saveAllAndFlush(List.of(expected, wrongAction));

        Specification<ProjectActivityLog> specification =
                ProjectActivitySpecification.hasProjectId(project.getId())
                        .and(ProjectActivitySpecification.hasEntityType(ActivityEntityType.TASK))
                        .and(ProjectActivitySpecification.hasAction(
                                ProjectActivityAction.TASK_STATUS_CHANGED
                        ))
                        .and(ProjectActivitySpecification.hasPerformedByUserId(actor.getId()))
                        .and(ProjectActivitySpecification.searchKeyword("oauth login"));

        assertThat(projectActivityLogRepository.findAll(specification))
                .extracting(ProjectActivityLog::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void findsStatusHistoryUpToCutoffInAscendingOrder() {
        User actor = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-activity-history",
                "repo-activity-history@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-ACTIVITY-HISTORY", "Activity history project", actor.getId()
        ));
        UUID taskId = UUID.randomUUID();
        ProjectActivityLog first = projectActivityLogRepository.saveAndFlush(
                TestEntityFactory.activity(
                        project.getId(), taskId, actor.getId(),
                        ProjectActivityAction.TASK_STATUS_CHANGED,
                        "{\"status\":\"IN_PROGRESS\"}"
                )
        );
        ProjectActivityLog second = projectActivityLogRepository.saveAndFlush(
                TestEntityFactory.activity(
                        project.getId(), taskId, actor.getId(),
                        ProjectActivityAction.TASK_STATUS_CHANGED,
                        "{\"status\":\"DONE\"}"
                )
        );

        assertThat(projectActivityLogRepository
                .findAllByProjectIdAndEntityTypeAndEntityIdInAndActionInAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                        project.getId(),
                        ActivityEntityType.TASK,
                        List.of(taskId),
                        List.of(ProjectActivityAction.TASK_STATUS_CHANGED),
                        java.time.Instant.now().plusSeconds(5)
                ))
                .extracting(ProjectActivityLog::getId)
                .containsExactly(first.getId(), second.getId());
    }
}
