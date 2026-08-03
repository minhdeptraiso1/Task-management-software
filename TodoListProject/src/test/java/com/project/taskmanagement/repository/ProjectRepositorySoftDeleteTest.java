package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectRepositorySoftDeleteTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired EntityManager entityManager;

    @Test
    void softDeletedProjectIsExcludedBySqlRestriction() {
        User user = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-soft-delete",
                "repo-soft-delete@test.local",
                UserRole.MANAGER
        ));
        Project project = projectRepository.saveAndFlush(
                TestEntityFactory.project(
                        "REPO-SOFT-DELETE",
                        "Soft delete project",
                        user.getId()
                )
        );

        project.markDeleted(user.getUsername());
        projectRepository.saveAndFlush(project);
        entityManager.clear();

        assertThat(projectRepository.findById(project.getId())).isEmpty();
        assertThat(projectRepository.findByCodeIgnoreCase(project.getCode())).isEmpty();
    }
}
