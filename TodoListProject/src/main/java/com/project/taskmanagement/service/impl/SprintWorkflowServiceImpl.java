package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.SprintMapper;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SprintWorkflowService;
import com.project.taskmanagement.service.TaskSprintSyncService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.SprintValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintWorkflowServiceImpl
        implements SprintWorkflowService {

    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskRepository taskRepository;

    SprintMapper sprintMapper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityService projectActivityService;
    NotificationService notificationService;
    TaskSprintSyncService taskSprintSyncService;

    // ===================== START =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SPRINT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CAPACITY, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_HEALTH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CLOSING_REPORT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.BACKLOG_ITEM_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_WORKLOAD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_SPRINT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_MEMBER, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_TIME, allEntries = true)
    })
    public SprintResponse start(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator.validateProjectEditable(
                project
        );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        if (sprint.getStatus()
                != SprintStatus.PLANNING) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_PLANNING
            );
        }

        boolean hasActiveSprint =
                sprintRepository
                        .existsByProjectIdAndStatus(
                                projectId,
                                SprintStatus.ACTIVE
                        );

        if (hasActiveSprint) {
            throw new BusinessException(
                    ErrorCode.SPRINT_ALREADY_ACTIVE
            );
        }

        long backlogItemCount =
                backlogItemRepository
                        .countByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        );

        if (backlogItemCount <= 0) {
            throw new BusinessException(
                    ErrorCode.SPRINT_EMPTY_CANNOT_START
            );
        }

        SprintStatus oldStatus =
                sprint.getStatus();

        sprint.setStatus(
                SprintStatus.ACTIVE
        );

        sprint.setStartedAt(
                Instant.now()
        );

        Sprint savedSprint =
                sprintRepository.save(
                        sprint
                );

        logSprintStatusChange(
                projectId,
                savedSprint,
                currentUser.getId(),
                ProjectActivityAction.SPRINT_STARTED,
                oldStatus,
                savedSprint.getStatus()
        );

        notifyProjectMembers(
                NotificationType.SPRINT_STARTED,
                "Sprint đã bắt đầu",
                "Sprint "
                        + savedSprint.getName()
                        + " đã bắt đầu",
                currentUser.getId(),
                projectId,
                savedSprint.getId()
        );

        return toResponse(savedSprint);
    }

    // ===================== COMPLETE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SPRINT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CAPACITY, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_HEALTH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CLOSING_REPORT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.BACKLOG_ITEM_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_WORKLOAD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_SPRINT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_MEMBER, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_TIME, allEntries = true)
    })
    public SprintResponse complete(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator.validateProjectEditable(
                project
        );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        if (sprint.getStatus()
                != SprintStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_ACTIVE
            );
        }

        return completeActiveSprint(
                projectId,
                sprint,
                currentUser
        );
    }

    private SprintResponse completeActiveSprint(
            UUID projectId,
            Sprint sprint,
            User currentUser
    ) {
        returnUnfinishedBacklogItems(
                projectId,
                sprint.getId()
        );

        taskSprintSyncService
                .handleSprintCompletion(
                        projectId,
                        sprint.getId(),
                        currentUser.getId()
                );

        SprintStatus oldStatus =
                sprint.getStatus();

        sprint.setStatus(
                SprintStatus.COMPLETED
        );

        sprint.setCompletedAt(
                Instant.now()
        );

        Sprint savedSprint =
                sprintRepository.save(
                        sprint
                );

        logSprintStatusChange(
                projectId,
                savedSprint,
                currentUser.getId(),
                ProjectActivityAction.SPRINT_COMPLETED,
                oldStatus,
                savedSprint.getStatus()
        );

        notifyProjectMembers(
                NotificationType.SPRINT_COMPLETED,
                "Sprint đã hoàn thành",
                "Sprint "
                        + savedSprint.getName()
                        + " đã hoàn thành",
                currentUser.getId(),
                projectId,
                savedSprint.getId()
        );

        return toResponse(savedSprint);
    }

    // ===================== CANCEL =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SPRINT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_KANBAN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_TASK_STATISTICS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_BURNDOWN, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CAPACITY, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_HEALTH, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_RISKS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_PROGRESS, allEntries = true),
            @CacheEvict(value = CacheNames.SPRINT_CLOSING_REPORT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.BACKLOG_ITEM_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.TASK_DETAIL, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_WORKLOAD, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_SPRINT, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_MEMBER, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_REPORT_TIME, allEntries = true)
    })
    public SprintResponse cancel(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator.validateProjectEditable(
                project
        );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        if (sprint.getStatus()
                != SprintStatus.PLANNING
                && sprint.getStatus()
                != SprintStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.SPRINT_CANNOT_CANCEL
            );
        }

        return cancelSprint(
                projectId,
                sprint,
                currentUser
        );
    }

    private SprintResponse cancelSprint(
            UUID projectId,
            Sprint sprint,
            User currentUser
    ) {
        returnUnfinishedBacklogItems(
                projectId,
                sprint.getId()
        );

        taskSprintSyncService
                .handleSprintCancellation(
                        projectId,
                        sprint.getId(),
                        currentUser.getId()
                );

        SprintStatus oldStatus =
                sprint.getStatus();

        sprint.setStatus(
                SprintStatus.CANCELLED
        );

        sprint.setCompletedAt(
                Instant.now()
        );

        Sprint savedSprint =
                sprintRepository.save(
                        sprint
                );

        logSprintStatusChange(
                projectId,
                savedSprint,
                currentUser.getId(),
                ProjectActivityAction.SPRINT_CANCELLED,
                oldStatus,
                savedSprint.getStatus()
        );

        notifyProjectMembers(
                NotificationType.SPRINT_CANCELLED,
                "Sprint đã bị hủy",
                "Sprint "
                        + savedSprint.getName()
                        + " đã bị hủy",
                currentUser.getId(),
                projectId,
                savedSprint.getId()
        );

        return toResponse(savedSprint);
    }

    // ===================== BACKLOG SYNC =====================

    private void returnUnfinishedBacklogItems(
            UUID projectId,
            UUID sprintId
    ) {
        List<BacklogItem> sprintItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        if (sprintItems.isEmpty()) {
            return;
        }

        Long maxBacklogPosition =
                backlogItemRepository
                        .findMaxBacklogPosition(
                                projectId
                        );

        long nextPosition =
                maxBacklogPosition == null
                        ? 1L
                        : maxBacklogPosition + 1L;

        List<BacklogItem> changedItems =
                new ArrayList<>();

        for (BacklogItem item : sprintItems) {
            if (item.getStatus()
                    == BacklogItemStatus.DONE) {

                continue;
            }

            item.setSprintId(null);

            item.setStatus(
                    BacklogItemStatus.READY
            );

            item.setPosition(
                    nextPosition++
            );

            changedItems.add(item);
        }

        if (!changedItems.isEmpty()) {
            backlogItemRepository.saveAll(
                    changedItems
            );
        }
    }

    // ===================== LOG =====================

    private void logSprintStatusChange(
            UUID projectId,
            Sprint sprint,
            UUID actorUserId,
            ProjectActivityAction action,
            SprintStatus oldStatus,
            SprintStatus newStatus
    ) {
        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "status",
                oldStatus
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "status",
                newStatus
        );

        newValue.put(
                "startedAt",
                sprint.getStartedAt()
        );

        newValue.put(
                "completedAt",
                sprint.getCompletedAt()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprint.getId(),
                        action,
                        actorUserId,
                        oldValue,
                        newValue
                )
        );
    }

    // ===================== NOTIFICATION =====================

    private void notifyProjectMembers(
            NotificationType type,
            String title,
            String content,
            UUID actorUserId,
            UUID projectId,
            UUID sprintId
    ) {
        List<UUID> recipientIds =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        )
                        .stream()
                        .map(ProjectMember::getUserId)
                        .distinct()
                        .toList();

        notificationService.create(
                new NotificationCommand(
                        type,
                        title,
                        content,
                        actorUserId,
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprintId,
                        recipientIds
                )
        );
    }

    // ===================== HELPER =====================

    private Sprint getSprintOrThrow(
            UUID projectId,
            UUID sprintId
    ) {
        return sprintRepository
                .findByIdAndProjectId(
                        sprintId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SPRINT_NOT_FOUND
                        )
                );
    }

    private SprintResponse toResponse(
            Sprint sprint
    ) {
        long backlogItemCount =
                backlogItemRepository
                        .countByProjectIdAndSprintId(
                                sprint.getProjectId(),
                                sprint.getId()
                        );

        long taskCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintId(
                                sprint.getProjectId(),
                                sprint.getId()
                        );

        long completedTaskCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintIdAndStatus(
                                sprint.getProjectId(),
                                sprint.getId(),
                                TaskStatus.DONE
                        );

        return sprintMapper.toResponse(
                sprint,
                backlogItemCount,
                taskCount,
                completedTaskCount,
                percentage(
                        completedTaskCount,
                        taskCount
                )
        );
    }

    private double percentage(
            long value,
            long total
    ) {
        if (total <= 0) {
            return 0.0;
        }

        return Math.round(
                value * 10000.0 / total
        ) / 100.0;
    }
}
