package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTimeLogRepositoryTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired SprintRepository sprintRepository;
    @Autowired BacklogItemRepository backlogItemRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired TaskTimeLogRepository taskTimeLogRepository;

    @Test
    void sumsMinutesByTaskUserDateAndProject() {
        User user = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-time-log",
                "repo-time-log@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-TIME-LOG",
                "Time log project",
                user.getId()
        ));
        Sprint sprint = sprintRepository.saveAndFlush(TestEntityFactory.sprint(
                project.getId(), "Time log sprint", SprintStatus.ACTIVE, user.getId()
        ));
        BacklogItem backlogItem = backlogItemRepository.saveAndFlush(
                TestEntityFactory.backlogItem(
                        project.getId(), sprint.getId(), "Time log story", 1L, user.getId()
                )
        );
        Task task = taskRepository.saveAndFlush(TestEntityFactory.task(
                project.getId(), backlogItem.getId(), sprint.getId(),
                "Time log task", TaskStatus.IN_PROGRESS, 1L, user.getId()
        ));
        LocalDate workDate = LocalDate.of(2026, 8, 3);
        List<TaskTimeLog> logs = taskTimeLogRepository.saveAllAndFlush(List.of(
                TestEntityFactory.timeLog(task.getId(), user.getId(), workDate, 30),
                TestEntityFactory.timeLog(task.getId(), user.getId(), workDate, 45),
                TestEntityFactory.timeLog(task.getId(), user.getId(), workDate.plusDays(1), 60)
        ));

        assertThat(taskTimeLogRepository.sumMinutesByTaskId(task.getId()))
                .isEqualTo(135L);
        assertThat(taskTimeLogRepository.sumMinutesByUserIdAndWorkDate(
                user.getId(), workDate
        )).isEqualTo(75L);
        assertThat(taskTimeLogRepository.sumProjectMinutes(project.getId()))
                .isEqualTo(135L);
        assertThat(taskTimeLogRepository.sumMinutesByUserIdAndWorkDateExcludingId(
                user.getId(), workDate, logs.get(0).getId()
        )).isEqualTo(45L);
    }
}
