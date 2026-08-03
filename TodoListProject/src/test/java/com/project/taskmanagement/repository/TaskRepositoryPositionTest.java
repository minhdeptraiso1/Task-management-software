package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRepositoryPositionTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired SprintRepository sprintRepository;
    @Autowired BacklogItemRepository backlogItemRepository;
    @Autowired TaskRepository taskRepository;

    @Test
    void returnsTasksOrderedByPositionInsideSprintColumn() {
        User user = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-position",
                "repo-position@test.local",
                UserRole.MANAGER
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-POSITION",
                "Position project",
                user.getId()
        ));
        Sprint sprint = sprintRepository.saveAndFlush(TestEntityFactory.sprint(
                project.getId(),
                "Position sprint",
                SprintStatus.ACTIVE,
                user.getId()
        ));
        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(),
                        sprint.getId(),
                        "Position story",
                        1L,
                        user.getId()
                )
        );

        taskRepository.saveAllAndFlush(List.of(
                TestEntityFactory.task(project.getId(), backlogItem.getId(), sprint.getId(),
                        "Third", TaskStatus.TODO, 3L, user.getId()),
                TestEntityFactory.task(project.getId(), backlogItem.getId(), sprint.getId(),
                        "First", TaskStatus.TODO, 1L, user.getId()),
                TestEntityFactory.task(project.getId(), backlogItem.getId(), sprint.getId(),
                        "Second", TaskStatus.TODO, 2L, user.getId()),
                TestEntityFactory.task(project.getId(), backlogItem.getId(), sprint.getId(),
                        "Other status", TaskStatus.IN_PROGRESS, 1L, user.getId())
        ));

        List<Task> tasks = taskRepository
                .findAllByProjectIdAndCurrentSprintIdAndStatusOrderByPositionAsc(
                        project.getId(),
                        sprint.getId(),
                        TaskStatus.TODO
                );

        assertThat(tasks)
                .extracting(Task::getTitle)
                .containsExactly("First", "Second", "Third");
        assertThat(taskRepository.findMaxSprintPosition(
                project.getId(), sprint.getId(), TaskStatus.TODO
        )).isEqualTo(3L);
    }
}
