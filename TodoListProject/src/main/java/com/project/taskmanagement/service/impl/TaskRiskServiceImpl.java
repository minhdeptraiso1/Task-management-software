package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.taskrisk.TaskRiskResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskScanResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskSummaryResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.TaskRiskLevel;
import com.project.taskmanagement.enums.TaskRiskReason;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskDependencyRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskRiskService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskRiskServiceImpl
        implements TaskRiskService {

    TaskRepository taskRepository;
    TaskDependencyRepository taskDependencyRepository;
    SprintRepository sprintRepository;
    UserRepository userRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    NotificationService notificationService;
    ProjectActivityService projectActivityService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DUE_SOON_DAYS = 3;
    static final int TOP_RISK_LIMIT = 10;
    static final int CRITICAL_BLOCKING_COUNT = 3;

    static final EnumSet<TaskStatus> TERMINAL_STATUSES =
            EnumSet.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    @Override
    @Transactional(readOnly = true)
    public List<TaskRiskResponse> getProjectTaskRisks(
            UUID projectId
    ) {
        requireViewAccess(projectId);

        return buildRiskResponses(
                projectId,
                taskRepository.findAllByProjectId(projectId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskRiskSummaryResponse getProjectRiskSummary(
            UUID projectId
    ) {
        requireViewAccess(projectId);

        return buildSummary(
                projectId,
                null,
                buildRiskResponses(
                        projectId,
                        taskRepository.findAllByProjectId(projectId)
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskRiskSummaryResponse getSprintRiskSummary(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(projectId);

        Sprint sprint =
                sprintRepository
                        .findByIdAndProjectId(
                                sprintId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SPRINT_NOT_FOUND
                                )
                        );

        return buildSummary(
                projectId,
                sprintId,
                buildRiskResponses(
                        projectId,
                        taskRepository.findAllByProjectIdAndCurrentSprintId(
                                projectId,
                                sprint.getId()
                        )
                )
        );
    }

    @Override
    @Transactional
    public TaskRiskScanResponse scanProjectRisks(
            UUID projectId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireTaskManagementAccess(
                projectId,
                currentUser
        );

        List<Task> tasks =
                taskRepository.findAllByProjectId(projectId);

        List<TaskRiskResponse> risks =
                buildRiskResponses(projectId, tasks);

        long notificationsCreated =
                sendRiskNotifications(
                        project,
                        currentUser,
                        risks
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        ProjectActivityAction.TASK_RISK_SCANNED,
                        currentUser.getId(),
                        null,
                        Map.of(
                                "scannedTasks",
                                tasks.size(),
                                "riskTasks",
                                risks.size(),
                                "notificationsCreated",
                                notificationsCreated
                        )
                )
        );

        return new TaskRiskScanResponse(
                projectId,
                tasks.size(),
                risks.size(),
                countLevel(risks, TaskRiskLevel.HIGH),
                countLevel(risks, TaskRiskLevel.CRITICAL),
                notificationsCreated,
                Instant.now()
        );
    }

    private List<TaskRiskResponse> buildRiskResponses(
            UUID projectId,
            List<Task> tasks
    ) {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        Map<UUID, User> usersById =
                loadAssignees(tasks);

        return tasks.stream()
                .map(task ->
                        buildRiskResponse(
                                projectId,
                                task,
                                usersById.get(task.getAssigneeUserId()),
                                today
                        )
                )
                .filter(response ->
                        response.riskLevel() != TaskRiskLevel.LOW
                                && response.riskLevel() != TaskRiskLevel.NONE
                )
                .sorted(this::compareRisk)
                .toList();
    }

    private TaskRiskResponse buildRiskResponse(
            UUID projectId,
            Task task,
            User assignee,
            LocalDate today
    ) {
        boolean terminal =
                TERMINAL_STATUSES.contains(task.getStatus());

        boolean blocked =
                task.getStatus() == TaskStatus.BLOCKED;

        boolean overdue =
                !terminal
                        && task.getDueDate() != null
                        && task.getDueDate().isBefore(today);

        boolean dueSoon =
                !terminal
                        && task.getDueDate() != null
                        && !task.getDueDate().isBefore(today)
                        && !task.getDueDate().isAfter(
                                today.plusDays(DUE_SOON_DAYS)
                        );

        long daysUntilDue =
                task.getDueDate() == null
                        ? 0L
                        : ChronoUnit.DAYS.between(
                                today,
                                task.getDueDate()
                        );

        long blockingTaskCount =
                taskDependencyRepository
                        .findBlockedTaskIdsByDependency(task.getId())
                        .size();

        long unresolvedDependencyCount =
                countUnresolvedDependencies(task);

        List<TaskRiskReason> reasons =
                new ArrayList<>();

        if (overdue && blocked) {
            reasons.add(TaskRiskReason.OVERDUE_AND_BLOCKED);
        } else {
            if (overdue) {
                reasons.add(TaskRiskReason.OVERDUE);
            }

            if (blocked) {
                reasons.add(TaskRiskReason.BLOCKED);
            }
        }

        if (dueSoon) {
            reasons.add(TaskRiskReason.DUE_SOON);
        }

        if (unresolvedDependencyCount > 0) {
            reasons.add(TaskRiskReason.DEPENDENCY_NOT_DONE);
        }

        if (blockingTaskCount > 0) {
            reasons.add(TaskRiskReason.BLOCKING_OTHER_TASKS);
        }

        if (reasons.isEmpty()) {
            reasons.add(TaskRiskReason.NONE);
        }

        TaskRiskLevel riskLevel =
                resolveRiskLevel(
                        overdue,
                        dueSoon,
                        blocked,
                        blockingTaskCount,
                        unresolvedDependencyCount
                );

        return new TaskRiskResponse(
                task.getId(),
                projectId,
                task.getCurrentSprintId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getAssigneeUserId(),
                assignee != null ? assignee.getUsername() : null,
                assignee != null ? assignee.getEmail() : null,
                task.getDueDate(),
                overdue,
                dueSoon,
                blocked,
                daysUntilDue,
                blockingTaskCount,
                unresolvedDependencyCount,
                riskLevel,
                reasons,
                "/projects/"
                        + projectId
                        + "/tasks/"
                        + task.getId()
        );
    }

    private TaskRiskLevel resolveRiskLevel(
            boolean overdue,
            boolean dueSoon,
            boolean blocked,
            long blockingTaskCount,
            long unresolvedDependencyCount
    ) {
        if (overdue && blocked) {
            return TaskRiskLevel.CRITICAL;
        }

        if (blockingTaskCount >= CRITICAL_BLOCKING_COUNT) {
            return TaskRiskLevel.CRITICAL;
        }

        if (overdue || blocked) {
            return TaskRiskLevel.HIGH;
        }

        if (unresolvedDependencyCount > 0) {
            return TaskRiskLevel.MEDIUM;
        }

        if (dueSoon) {
            return TaskRiskLevel.MEDIUM;
        }

        return TaskRiskLevel.LOW;
    }

    private TaskRiskSummaryResponse buildSummary(
            UUID projectId,
            UUID sprintId,
            List<TaskRiskResponse> risks
    ) {
        return new TaskRiskSummaryResponse(
                projectId,
                sprintId,
                risks.size(),
                countLevel(risks, TaskRiskLevel.LOW),
                countLevel(risks, TaskRiskLevel.MEDIUM),
                countLevel(risks, TaskRiskLevel.HIGH),
                countLevel(risks, TaskRiskLevel.CRITICAL),
                risks.stream().filter(TaskRiskResponse::overdue).count(),
                risks.stream().filter(TaskRiskResponse::dueSoon).count(),
                risks.stream().filter(TaskRiskResponse::blocked).count(),
                risks.stream()
                        .filter(response ->
                                response.unresolvedDependencyCount() > 0
                        )
                        .count(),
                risks.stream()
                        .limit(TOP_RISK_LIMIT)
                        .toList()
        );
    }

    private long sendRiskNotifications(
            Project project,
            User actor,
            List<TaskRiskResponse> risks
    ) {
        long created = 0L;

        for (TaskRiskResponse risk : risks) {
            if (risk.assigneeUserId() == null) {
                continue;
            }

            NotificationType type =
                    resolveNotificationType(risk);

            if (type == null) {
                continue;
            }

            notificationService.create(
                    new NotificationCommand(
                            type,
                            resolveNotificationTitle(risk),
                            resolveNotificationContent(project, risk),
                            actor.getId(),
                            project.getId(),
                            ActivityEntityType.TASK,
                            risk.taskId(),
                            List.of(risk.assigneeUserId())
                    )
            );

            created++;
        }

        return created;
    }

    private NotificationType resolveNotificationType(
            TaskRiskResponse risk
    ) {
        if (risk.riskLevel() == TaskRiskLevel.CRITICAL) {
            return NotificationType.TASK_RISK_CRITICAL;
        }

        if (risk.riskLevel() == TaskRiskLevel.HIGH) {
            return NotificationType.TASK_RISK_HIGH;
        }

        if (risk.overdue()) {
            return NotificationType.TASK_OVERDUE;
        }

        if (risk.dueSoon()) {
            return NotificationType.TASK_DUE_SOON;
        }

        if (risk.unresolvedDependencyCount() > 0) {
            return NotificationType.TASK_DEPENDENCY_RISK;
        }

        return null;
    }

    private String resolveNotificationTitle(
            TaskRiskResponse risk
    ) {
        return switch (risk.riskLevel()) {
            case CRITICAL -> "Task co rui ro nghiem trong";
            case HIGH -> "Task co rui ro cao";
            case MEDIUM -> "Task can duoc theo doi";
            case LOW, NONE -> "Task can theo doi";
        };
    }

    private String resolveNotificationContent(
            Project project,
            TaskRiskResponse risk
    ) {
        return "Task "
                + risk.title()
                + " trong du an "
                + project.getName()
                + " dang co muc rui ro "
                + risk.riskLevel();
    }

    private long countUnresolvedDependencies(
            Task task
    ) {
        List<UUID> dependencyIds =
                taskDependencyRepository
                        .findDependsOnTaskIds(task.getId());

        if (dependencyIds.isEmpty()) {
            return 0L;
        }

        return taskRepository
                .findAllById(dependencyIds)
                .stream()
                .filter(dependency ->
                        !TERMINAL_STATUSES.contains(dependency.getStatus())
                )
                .count();
    }

    private Project requireViewAccess(
            UUID projectId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(
                project,
                currentUser
        );

        return project;
    }

    private Map<UUID, User> loadAssignees(
            List<Task> tasks
    ) {
        List<UUID> userIds =
                tasks.stream()
                        .map(Task::getAssigneeUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        if (userIds.isEmpty()) {
            return new LinkedHashMap<>();
        }

        return userRepository
                .findAllById(userIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                User::getId,
                                Function.identity(),
                                (left, right) -> left,
                                LinkedHashMap::new
                        )
                );
    }

    private int compareRisk(
            TaskRiskResponse left,
            TaskRiskResponse right
    ) {
        int levelCompare =
                Integer.compare(
                        riskWeight(right.riskLevel()),
                        riskWeight(left.riskLevel())
                );

        if (levelCompare != 0) {
            return levelCompare;
        }

        return Comparator
                .nullsLast(LocalDate::compareTo)
                .compare(left.dueDate(), right.dueDate());
    }

    private int riskWeight(
            TaskRiskLevel level
    ) {
        return switch (level) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
            case NONE -> 0;
        };
    }

    private long countLevel(
            List<TaskRiskResponse> risks,
            TaskRiskLevel level
    ) {
        return risks.stream()
                .filter(risk -> risk.riskLevel() == level)
                .count();
    }
}
