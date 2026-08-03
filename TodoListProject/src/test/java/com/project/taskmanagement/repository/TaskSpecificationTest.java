package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.spec.TaskSpecification;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskSpecificationTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired SprintRepository sprintRepository;
    @Autowired BacklogItemRepository backlogItemRepository;
    @Autowired TaskRepository taskRepository;

    @Test
    void combinesProjectKeywordStatusAndPriorityFilters() {
        User user = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-task-spec",
                "repo-task-spec@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-TASK-SPEC",
                "Task specification project",
                user.getId()
        ));
        Sprint sprint = sprintRepository.saveAndFlush(TestEntityFactory.sprint(
                project.getId(), "Task specification sprint", SprintStatus.ACTIVE, user.getId()
        ));
        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(), sprint.getId(), "Search story", 1L, user.getId()
                )
        );
        Task expected = TestEntityFactory.task(
                project.getId(), backlogItem.getId(), sprint.getId(),
                "Implement OAuth login", TaskStatus.IN_PROGRESS, 1L, user.getId()
        );
        expected.setPriority(TaskPriority.HIGH);
        Task wrongStatus = TestEntityFactory.task(
                project.getId(), backlogItem.getId(), sprint.getId(),
                "Implement OAuth logout", TaskStatus.DONE, 2L, user.getId()
        );
        wrongStatus.setPriority(TaskPriority.HIGH);
        Task wrongKeyword = TestEntityFactory.task(
                project.getId(), backlogItem.getId(), sprint.getId(),
                "Write documentation", TaskStatus.IN_PROGRESS, 3L, user.getId()
        );
        wrongKeyword.setPriority(TaskPriority.HIGH);
        taskRepository.saveAllAndFlush(List.of(expected, wrongStatus, wrongKeyword));

        Specification<Task> specification =
                TaskSpecification.belongsToProject(project.getId())
                        .and(TaskSpecification.search("oauth login"))
                        .and(TaskSpecification.hasStatus(TaskStatus.IN_PROGRESS))
                        .and(TaskSpecification.hasPriority(TaskPriority.HIGH));

        assertThat(taskRepository.findAll(specification))
                .extracting(Task::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void overdueFilterExcludesDoneTasks() {
        User user = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-overdue-spec",
                "repo-overdue-spec@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-OVERDUE-SPEC", "Overdue project", user.getId()
        ));
        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(), null, "Overdue story", 1L, user.getId()
                )
        );
        Task overdue = TestEntityFactory.task(
                project.getId(), backlogItem.getId(), null,
                "Open overdue task", TaskStatus.TODO, 1L, user.getId()
        );
        Task done = TestEntityFactory.task(
                project.getId(), backlogItem.getId(), null,
                "Done overdue task", TaskStatus.DONE, 2L, user.getId()
        );
        taskRepository.saveAllAndFlush(List.of(overdue, done));

        Specification<Task> specification =
                TaskSpecification.belongsToProject(project.getId())
                        .and(TaskSpecification.overdueOnly(
                                true,
                                java.time.LocalDate.of(2026, 8, 10)
                        ));

        assertThat(taskRepository.findAll(specification))
                .extracting(Task::getId)
                .containsExactly(overdue.getId());
    }
}
