package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SprintRepositoryTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired SprintRepository sprintRepository;

    @Test
    void allowsOnlyOneActiveSprintPerProject() {
        User user = persistManager("active");
        Project project = persistProject("ACTIVE", user);

        Sprint first = sprintRepository.saveAndFlush(
                TestEntityFactory.sprint(
                        project.getId(),
                        "Sprint active 1",
                        SprintStatus.ACTIVE,
                        user.getId()
                )
        );
        assertThat(first.getId()).isNotNull();

        Sprint second = TestEntityFactory.sprint(
                project.getId(),
                "Sprint active 2",
                SprintStatus.ACTIVE,
                user.getId()
        );

        assertThatThrownBy(() -> sprintRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsMultiplePlanningSprintsInSameProject() {
        User user = persistManager("planning");
        Project project = persistProject("PLANNING", user);

        sprintRepository.saveAndFlush(TestEntityFactory.sprint(
                project.getId(),
                "Sprint planning 1",
                SprintStatus.PLANNING,
                user.getId()
        ));
        sprintRepository.saveAndFlush(TestEntityFactory.sprint(
                project.getId(),
                "Sprint planning 2",
                SprintStatus.PLANNING,
                user.getId()
        ));

        assertThat(sprintRepository.findAllByProjectIdOrderByCreatedAtDesc(
                project.getId()
        )).hasSize(2);
    }

    private User persistManager(String suffix) {
        return userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-sprint-" + suffix,
                "repo-sprint-" + suffix + "@test.local",
                UserRole.MANAGER
        ));
    }

    private Project persistProject(String suffix, User user) {
        return projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-SPRINT-" + suffix,
                "Repository sprint project " + suffix,
                user.getId()
        ));
    }
}
