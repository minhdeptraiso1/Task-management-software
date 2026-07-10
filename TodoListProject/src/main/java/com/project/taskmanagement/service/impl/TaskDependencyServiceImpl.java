package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.task.BlockTaskRequest;
import com.project.taskmanagement.dto.request.task.CreateTaskDependencyRequest;
import com.project.taskmanagement.dto.request.task.UnblockTaskRequest;
import com.project.taskmanagement.dto.response.task.ProjectTaskRiskPageResponse;
import com.project.taskmanagement.dto.response.task.TaskDependencyResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.dto.response.task.TaskRiskResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskDependency;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.TaskRiskLevel;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TaskDependencyRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskDependencyService;
import com.project.taskmanagement.service.TaskService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.TaskWorkflowValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskDependencyServiceImpl
        implements TaskDependencyService {

    TaskDependencyRepository taskDependencyRepository;
    TaskRepository taskRepository;

    TaskService taskService;
    ProjectActivityService projectActivityService;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.MY_TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true)
    })
    public TaskDependencyResponse addDependency(
            UUID projectId,
            UUID taskId,
            CreateTaskDependencyRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        projectAccessService.requireTaskManagementAccess(
                projectId,
                currentUser
        );

        Task task =
                getTaskOrThrow(projectId, taskId);

        UUID dependsOnTaskId =
                request.dependsOnTaskId();

        if (taskId.equals(dependsOnTaskId)) {
            throw new BusinessException(
                    ErrorCode.TASK_DEPENDENCY_SELF_NOT_ALLOWED
            );
        }

        Task dependsOnTask =
                getTaskOrThrow(projectId, dependsOnTaskId);

        if (!task.getProjectId().equals(dependsOnTask.getProjectId())) {
            throw new BusinessException(
                    ErrorCode.TASK_DEPENDENCY_CROSS_PROJECT_NOT_ALLOWED
            );
        }

        if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(
                taskId,
                dependsOnTaskId
        )) {
            throw new BusinessException(
                    ErrorCode.TASK_DEPENDENCY_ALREADY_EXISTS
            );
        }

        validateNoCycle(taskId, dependsOnTaskId);

        TaskDependency dependency =
                TaskDependency.builder()
                        .taskId(taskId)
                        .dependsOnTaskId(dependsOnTaskId)
                        .build();

        TaskDependency savedDependency =
                taskDependencyRepository.save(dependency);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        taskId,
                        ProjectActivityAction.TASK_DEPENDENCY_ADDED,
                        currentUser.getId(),
                        null,
                        Map.of(
                                "dependsOnTaskId",
                                dependsOnTaskId
                        )
                )
        );

        return toDependencyResponse(
                savedDependency,
                dependsOnTask
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> getDependencies(
            UUID projectId,
            UUID taskId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireViewAccess(projectId, currentUser);

        getTaskOrThrow(projectId, taskId);

        return taskDependencyRepository
                .findAllByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(dependency ->
                        toDependencyResponse(
                                dependency,
                                getTaskOrThrow(
                                        projectId,
                                        dependency.getDependsOnTaskId()
                                )
                        )
                )
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.MY_TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true)
    })
    public void removeDependency(
            UUID projectId,
            UUID taskId,
            UUID dependencyId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        projectAccessService.requireTaskManagementAccess(
                projectId,
                currentUser
        );

        getTaskOrThrow(projectId, taskId);

        TaskDependency dependency =
                taskDependencyRepository
                        .findByIdAndTaskId(
                                dependencyId,
                                taskId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TASK_DEPENDENCY_NOT_FOUND
                                )
                        );

        dependency.markDeleted(currentUser.getUsername());
        taskDependencyRepository.save(dependency);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        taskId,
                        ProjectActivityAction.TASK_DEPENDENCY_REMOVED,
                        currentUser.getId(),
                        Map.of(
                                "dependsOnTaskId",
                                dependency.getDependsOnTaskId()
                        ),
                        null
                )
        );
    }

    @Override
    @Transactional
    public TaskResponse blockTask(
            UUID projectId,
            UUID taskId,
            BlockTaskRequest request
    ) {
        return taskService.block(
                projectId,
                taskId,
                request
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.MY_TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true)
    })
    public TaskResponse unblockTask(
            UUID projectId,
            UUID taskId,
            UnblockTaskRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        projectAccessService.requireTaskManagementAccess(
                projectId,
                currentUser
        );

        Task task =
                getTaskOrThrow(projectId, taskId);

        if (task.getStatus() != TaskStatus.BLOCKED) {
            throw new BusinessException(
                    ErrorCode.TASK_NOT_BLOCKED
            );
        }

        TaskStatus targetStatus =
                resolveUnblockTargetStatus(request);

        TaskWorkflowValidator.validateTransition(
                TaskStatus.BLOCKED,
                targetStatus
        );

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put("status", task.getStatus());
        oldValue.put("blockReason", task.getBlockReason());
        oldValue.put("blockedAt", task.getBlockedAt());
        oldValue.put("blockedByUserId", task.getBlockedByUserId());

        task.setStatus(targetStatus);
        task.setBlockReason(null);
        task.setBlockedAt(null);
        task.setBlockedByUserId(null);
        task.setCompletedAt(null);

        taskRepository.save(task);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        taskId,
                        ProjectActivityAction.TASK_UNBLOCKED,
                        currentUser.getId(),
                        oldValue,
                        Map.of("status", targetStatus)
                )
        );

        return taskService.getById(projectId, taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskRiskResponse getTaskRisk(
            UUID projectId,
            UUID taskId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireViewAccess(projectId, currentUser);

        return buildRiskResponse(
                getTaskOrThrow(projectId, taskId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectTaskRiskPageResponse getProjectTaskRisks(
            UUID projectId,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireViewAccess(projectId, currentUser);

        Page<TaskRiskResponse> page =
                taskRepository
                        .findAllByProjectId(
                                projectId,
                                pageable
                        )
                        .map(this::buildRiskResponse);

        return ProjectTaskRiskPageResponse.from(page);
    }

    private TaskRiskResponse buildRiskResponse(
            Task task
    ) {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        boolean terminal =
                task.getStatus() == TaskStatus.DONE
                        || task.getStatus() == TaskStatus.CANCELLED;

        boolean blocked =
                task.getStatus() == TaskStatus.BLOCKED;

        boolean overdue =
                task.getDueDate() != null
                        && task.getDueDate().isBefore(today)
                        && !terminal;

        long unfinishedDependencyCount =
                countUnfinishedDependencies(task);

        boolean hasUnfinishedDependencies =
                unfinishedDependencyCount > 0;

        List<String> reasons =
                new ArrayList<>();

        TaskRiskLevel riskLevel;

        if (terminal) {
            riskLevel = TaskRiskLevel.NONE;
            reasons.add("Task da ket thuc");
        } else if (blocked || overdue) {
            riskLevel = TaskRiskLevel.HIGH;
            if (blocked) {
                reasons.add(
                        task.getBlockReason() == null
                                || task.getBlockReason().isBlank()
                                ? "Task dang bi chan"
                                : task.getBlockReason()
                );
            }
            if (overdue) {
                reasons.add("Task qua han");
            }
        } else if (isDueSoon(task, today)
                || hasUnfinishedDependencies) {

            riskLevel = TaskRiskLevel.MEDIUM;

            if (isDueSoon(task, today)) {
                reasons.add("Task sap den han");
            }

            if (hasUnfinishedDependencies) {
                reasons.add(
                        "Con "
                                + unfinishedDependencyCount
                                + " dependency chua hoan thanh"
                );
            }
        } else {
            riskLevel = TaskRiskLevel.LOW;
            reasons.add(
                    task.getDueDate() == null
                            ? "Task chua co han hoan thanh"
                            : "Task dang binh thuong"
            );
        }

        return new TaskRiskResponse(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getStatus(),
                task.getDueDate(),
                riskLevel,
                overdue,
                blocked,
                hasUnfinishedDependencies,
                unfinishedDependencyCount,
                reasons,
                "/projects/"
                        + task.getProjectId()
                        + "/tasks/"
                        + task.getId()
        );
    }

    private long countUnfinishedDependencies(
            Task task
    ) {
        List<UUID> dependencyIds =
                taskDependencyRepository
                        .findDependsOnTaskIds(
                                task.getId()
                        );

        if (dependencyIds.isEmpty()) {
            return 0L;
        }

        return taskRepository
                .findAllById(dependencyIds)
                .stream()
                .filter(dependency ->
                        dependency.getStatus() != TaskStatus.DONE
                                && dependency.getStatus() != TaskStatus.CANCELLED
                )
                .count();
    }

    private boolean isDueSoon(
            Task task,
            LocalDate today
    ) {
        if (task.getDueDate() == null) {
            return false;
        }

        return !task.getDueDate().isBefore(today)
                && !task.getDueDate().isAfter(today.plusDays(1));
    }

    private void validateNoCycle(
            UUID taskId,
            UUID dependsOnTaskId
    ) {
        Set<UUID> visited =
                new HashSet<>();

        Queue<UUID> queue =
                new ArrayDeque<>();

        queue.add(dependsOnTaskId);

        while (!queue.isEmpty()) {
            UUID current =
                    queue.poll();

            if (!visited.add(current)) {
                continue;
            }

            if (current.equals(taskId)) {
                throw new BusinessException(
                        ErrorCode.TASK_DEPENDENCY_CYCLE_DETECTED
                );
            }

            queue.addAll(
                    taskDependencyRepository
                            .findDependsOnTaskIds(current)
            );
        }
    }

    private void requireViewAccess(
            UUID projectId,
            User currentUser
    ) {
        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(
                project,
                currentUser
        );
    }

    private Task getTaskOrThrow(
            UUID projectId,
            UUID taskId
    ) {
        return taskRepository
                .findByIdAndProjectId(
                        taskId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TASK_NOT_FOUND
                        )
                );
    }

    private TaskStatus resolveUnblockTargetStatus(
            UnblockTaskRequest request
    ) {
        TaskStatus targetStatus =
                request == null
                        || request.targetStatus() == null
                        ? TaskStatus.TODO
                        : request.targetStatus();

        if (targetStatus != TaskStatus.TODO
                && targetStatus != TaskStatus.IN_PROGRESS) {

            throw new BusinessException(
                    ErrorCode.TASK_UNBLOCK_TARGET_STATUS_INVALID
            );
        }

        return targetStatus;
    }

    private TaskDependencyResponse toDependencyResponse(
            TaskDependency dependency,
            Task dependsOnTask
    ) {
        boolean completed =
                dependsOnTask.getStatus() == TaskStatus.DONE
                        || dependsOnTask.getStatus() == TaskStatus.CANCELLED;

        return new TaskDependencyResponse(
                dependency.getId(),
                dependency.getTaskId(),
                dependsOnTask.getId(),
                dependsOnTask.getTitle(),
                dependsOnTask.getStatus(),
                dependsOnTask.getPriority(),
                dependsOnTask.getAssigneeUserId(),
                dependsOnTask.getDueDate(),
                completed,
                dependency.getCreatedAt()
        );
    }
}
