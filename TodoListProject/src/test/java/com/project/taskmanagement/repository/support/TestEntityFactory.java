package com.project.taskmanagement.repository.support;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.ProjectStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;
import com.project.taskmanagement.enums.UserRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class TestEntityFactory {

    private TestEntityFactory() {
    }

    public static User user(
            String username,
            String email,
            UserRole role
    ) {
        return User.builder()
                .username(username)
                .email(email)
                .password("encoded-password")
                .role(role)
                .enabled(true)
                .build();
    }

    public static Project project(
            String code,
            String name,
            UUID createdByUserId
    ) {
        return Project.builder()
                .code(code)
                .name(name)
                .description("Test project")
                .status(ProjectStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .createdByUserId(createdByUserId)
                .build();
    }

    public static ProjectMember projectMember(
            UUID projectId,
            UUID userId,
            ProjectMemberRole role
    ) {
        return ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .role(role)
                .joinedAt(Instant.now())
                .build();
    }

    public static Sprint sprint(
            UUID projectId,
            String name,
            SprintStatus status,
            UUID createdByUserId
    ) {
        return Sprint.builder()
                .projectId(projectId)
                .name(name)
                .goal("Test sprint goal")
                .status(status)
                .startDate(LocalDate.of(2026, 8, 3))
                .endDate(LocalDate.of(2026, 8, 9))
                .createdByUserId(createdByUserId)
                .build();
    }

    public static BacklogItem backlogItem(
            UUID projectId,
            UUID sprintId,
            String title,
            long position,
            UUID createdByUserId
    ) {
        return BacklogItem.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .title(title)
                .description("Test backlog item")
                .type(BacklogItemType.USER_STORY)
                .status(sprintId == null
                        ? BacklogItemStatus.READY
                        : BacklogItemStatus.IN_SPRINT)
                .priority(BacklogPriority.MEDIUM)
                .storyPoints(3)
                .position(position)
                .createdByUserId(createdByUserId)
                .build();
    }

    public static Task task(
            UUID projectId,
            UUID backlogItemId,
            UUID sprintId,
            String title,
            TaskStatus status,
            long position,
            UUID reporterUserId
    ) {
        return Task.builder()
                .projectId(projectId)
                .backlogItemId(backlogItemId)
                .currentSprintId(sprintId)
                .originSprintId(sprintId)
                .title(title)
                .description("Test task")
                .type(TaskType.DEVELOPMENT)
                .priority(TaskPriority.MEDIUM)
                .status(status)
                .reporterUserId(reporterUserId)
                .estimatedMinutes(120)
                .startDate(LocalDate.of(2026, 8, 3))
                .dueDate(LocalDate.of(2026, 8, 6))
                .position(position)
                .build();
    }

    public static TaskTimeLog timeLog(
            UUID taskId,
            UUID userId,
            LocalDate workDate,
            int minutes
    ) {
        return TaskTimeLog.builder()
                .taskId(taskId)
                .userId(userId)
                .workDate(workDate)
                .minutes(minutes)
                .description("Test time log")
                .build();
    }

    public static Notification notification(
            UUID actorUserId,
            UUID projectId,
            UUID entityId
    ) {
        return Notification.builder()
                .type(NotificationType.TASK_STATUS_CHANGED)
                .title("Task status changed")
                .content("A task changed status")
                .actorUserId(actorUserId)
                .projectId(projectId)
                .entityType(ActivityEntityType.TASK)
                .entityId(entityId)
                .build();
    }

    public static NotificationRecipient recipient(
            UUID notificationId,
            UUID userId,
            Instant readAt
    ) {
        return NotificationRecipient.builder()
                .notificationId(notificationId)
                .userId(userId)
                .deliveredAt(Instant.now())
                .readAt(readAt)
                .build();
    }

    public static ProjectActivityLog activity(
            UUID projectId,
            UUID entityId,
            UUID actorUserId,
            ProjectActivityAction action,
            String newValueJson
    ) {
        return ProjectActivityLog.builder()
                .projectId(projectId)
                .entityType(ActivityEntityType.TASK)
                .entityId(entityId)
                .action(action)
                .performedByUserId(actorUserId)
                .newValueJson(newValueJson)
                .build();
    }
}
