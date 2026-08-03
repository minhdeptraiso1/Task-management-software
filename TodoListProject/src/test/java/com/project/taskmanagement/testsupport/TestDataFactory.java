package com.project.taskmanagement.testsupport;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;
import com.project.taskmanagement.enums.UserRole;

import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User admin() {
        return user(UserRole.ADMIN);
    }

    public static User employee() {
        return user(UserRole.EMPLOYEE);
    }

    public static User user(UserRole role) {
        return User.builder()
                .username("user-" + UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.local")
                .password("encoded-password")
                .role(role)
                .enabled(true)
                .build();
    }

    public static Project activeProject() {
        return Project.builder()
                .code("PRJ-" + UUID.randomUUID())
                .name("Test project")
                .status(ProjectStatus.ACTIVE)
                .createdByUserId(UUID.randomUUID())
                .build();
    }

    public static ProjectMember membership(
            Project project,
            User user,
            ProjectMemberRole role
    ) {
        return ProjectMember.builder()
                .projectId(project.getId())
                .userId(user.getId())
                .role(role)
                .build();
    }

    public static Sprint sprint(
            Project project,
            SprintStatus status
    ) {
        return Sprint.builder()
                .projectId(project.getId())
                .name("Sprint test")
                .status(status)
                .createdByUserId(UUID.randomUUID())
                .build();
    }

    public static Task task(
            Project project,
            Sprint sprint,
            TaskStatus status
    ) {
        return Task.builder()
                .projectId(project.getId())
                .backlogItemId(UUID.randomUUID())
                .currentSprintId(sprint == null ? null : sprint.getId())
                .originSprintId(sprint == null ? null : sprint.getId())
                .title("Task test")
                .type(TaskType.DEVELOPMENT)
                .status(status)
                .priority(TaskPriority.MEDIUM)
                .reporterUserId(UUID.randomUUID())
                .position(1L)
                .build();
    }
}
