package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.sprint.CreateSprintRequest;
import com.project.taskmanagement.dto.request.sprint.SprintSearchRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintRequest;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.dto.response.sprint.SprintPageResponse;
import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.BacklogItemMapper;
import com.project.taskmanagement.mapper.SprintMapper;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.spec.SprintSpecification;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.SprintService;
import com.project.taskmanagement.service.TaskSprintSyncService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.SprintValidator;
import com.project.taskmanagement.util.TextNormalizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintServiceImpl
        implements SprintService {

    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    SprintMapper sprintMapper;

    ProjectAccessService projectAccessService;
    CurrentUserService currentUserService;
    ProjectActivityService projectActivityService;

    BacklogItemMapper backlogItemMapper;
    NotificationService notificationService;
    ProjectMemberRepository projectMemberRepository;
    TaskSprintSyncService taskSprintSyncService;

    // ===================== CREATE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public SprintResponse create(
            UUID projectId,
            CreateSprintRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator
                .validateProjectEditable(project);

        SprintValidator.validateDates(
                request.startDate(),
                request.endDate()
        );

        String normalizedName =
                TextNormalizer.trim(
                        request.name()
                );

        if (sprintRepository
                .existsByProjectIdAndNameIgnoreCase(
                        projectId,
                        normalizedName
                )) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NAME_ALREADY_EXISTS
            );
        }

        Sprint sprint =
                Sprint.builder()
                        .projectId(projectId)
                        .name(normalizedName)
                        .goal(
                                TextNormalizer.trimToNull(
                                        request.goal()
                                )
                        )
                        .status(SprintStatus.PLANNING)
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startedAt(null)
                        .completedAt(null)
                        .createdByUserId(
                                currentUser.getId()
                        )
                        .build();

        Sprint savedSprint =
                sprintRepository.save(sprint);

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "name",
                savedSprint.getName()
        );
        newValue.put(
                "goal",
                savedSprint.getGoal()
        );
        newValue.put(
                "status",
                savedSprint.getStatus()
        );
        newValue.put(
                "startDate",
                savedSprint.getStartDate()
        );
        newValue.put(
                "endDate",
                savedSprint.getEndDate()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        ProjectActivityAction.SPRINT_CREATED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.SPRINT_CREATED,
                        "Sprint mới đã được tạo",
                        savedSprint.getName()
                                + " đã được tạo trong dự án",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        getProjectRecipientIds(projectId)
                )
        );

        return toResponse(savedSprint);
    }

    // ===================== SEARCH =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword())" +
                    " + '|status=' + (#request == null || #request.status() == null ? '' : #request.status())" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public SprintPageResponse search(
            UUID projectId,
            SprintSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        String keyword =
                request != null
                        ? request.keyword()
                        : null;

        SprintStatus status =
                request != null
                        ? request.status()
                        : null;

        Specification<Sprint> specification =
                Specification.allOf(
                        SprintSpecification
                                .belongsToProject(projectId),
                        SprintSpecification
                                .search(keyword),
                        SprintSpecification
                                .hasStatus(status)
                );

        Page<SprintResponse> responsePage =
                sprintRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toResponse);

        return SprintPageResponse.from(
                responsePage
        );
    }

    // ===================== DETAIL =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #sprintId"
    )
    public SprintResponse getById(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        return toResponse(sprint);
    }

    // ===================== UPDATE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public SprintResponse update(
            UUID projectId,
            UUID sprintId,
            UpdateSprintRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator
                .validateProjectEditable(project);

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validateEditable(
                sprint
        );

        Map<String, Object> oldValue =
                sprintSnapshot(sprint);

        String newName =
                request.name() != null
                        ? TextNormalizer.trim(
                        request.name()
                )
                        : sprint.getName();

        if (newName == null
                || newName.isBlank()) {

            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR
            );
        }

        if (!newName.equalsIgnoreCase(
                sprint.getName()
        ) && sprintRepository
                .existsByProjectIdAndNameIgnoreCaseAndIdNot(
                        projectId,
                        newName,
                        sprintId
                )) {

            throw new BusinessException(
                    ErrorCode.SPRINT_NAME_ALREADY_EXISTS
            );
        }

        var newStartDate =
                request.startDate() != null
                        ? request.startDate()
                        : sprint.getStartDate();

        var newEndDate =
                request.endDate() != null
                        ? request.endDate()
                        : sprint.getEndDate();

        SprintValidator.validateDates(
                newStartDate,
                newEndDate
        );

        if (request.name() != null) {
            sprint.setName(newName);
        }

        if (request.goal() != null) {
            sprint.setGoal(
                    TextNormalizer.trimToNull(
                            request.goal()
                    )
            );
        }

        if (request.startDate() != null) {
            sprint.setStartDate(
                    request.startDate()
            );
        }

        if (request.endDate() != null) {
            sprint.setEndDate(
                    request.endDate()
            );
        }

        Sprint savedSprint =
                sprintRepository.save(sprint);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        ProjectActivityAction.SPRINT_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        sprintSnapshot(savedSprint)
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.SPRINT_UPDATED,
                        "Sprint đã được cập nhật",
                        savedSprint.getName()
                                + " đã được cập nhật",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        getProjectRecipientIds(projectId)
                )
        );

        return toResponse(savedSprint);
    }

    // ===================== DELETE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public void delete(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator
                .validateProjectEditable(project);

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validateEditable(
                sprint
        );

        boolean hasBacklogItems =
                backlogItemRepository
                        .existsByProjectIdAndSprintId(
                                projectId,
                                sprintId
                        );

        if (hasBacklogItems) {
            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_EMPTY
            );
        }

        Map<String, Object> oldValue =
                sprintSnapshot(sprint);

        sprint.markDeleted(
                currentUser.getUsername()
        );

        sprintRepository.save(sprint);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprintId,
                        ProjectActivityAction.SPRINT_CANCELLED,
                        currentUser.getId(),
                        oldValue,
                        Map.of(
                                "deleted",
                                true
                        )
                )
        );
    }

    //====================== Đưa Item vào Sprint =============================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public BacklogItemResponse addBacklogItem(
            UUID projectId,
            UUID sprintId,
            UUID itemId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator
                .validateProjectEditable(project);

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validatePlanning(
                sprint
        );

        BacklogItem backlogItem =
                backlogItemRepository
                        .findByIdAndProjectId(
                                itemId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BACKLOG_ITEM_NOT_FOUND
                                )
                        );

        if (!backlogItem.getProjectId()
                .equals(sprint.getProjectId())) {

            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_SPRINT_MISMATCH
            );
        }

        if (backlogItem.getSprintId() != null) {
            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_ALREADY_IN_SPRINT
            );
        }

        if (backlogItem.getStatus()
                != BacklogItemStatus.READY) {

            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_NOT_READY
            );
        }

        Long oldPosition =
                backlogItem.getPosition();

        backlogItemRepository
                .shiftPositionsDown(
                        projectId,
                        null,
                        oldPosition
                );

        Long maxSprintPosition =
                backlogItemRepository
                        .findMaxSprintPosition(
                                projectId,
                                sprintId
                        );

        long newPosition =
                maxSprintPosition == null
                        ? 1L
                        : maxSprintPosition + 1L;

        backlogItem.setSprintId(
                sprintId
        );

        backlogItem.setStatus(
                BacklogItemStatus.IN_SPRINT
        );

        backlogItem.setPosition(
                newPosition
        );

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        taskSprintSyncService
                .attachBacklogItemTasksToSprint(
                        projectId,
                        savedItem.getId(),
                        sprintId,
                        currentUser.getId()
                );

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "sprintId",
                null
        );
        oldValue.put(
                "status",
                BacklogItemStatus.READY
        );
        oldValue.put(
                "position",
                oldPosition
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "sprintId",
                sprintId
        );
        newValue.put(
                "status",
                BacklogItemStatus.IN_SPRINT
        );
        newValue.put(
                "position",
                newPosition
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_ADDED_TO_SPRINT,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

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
                        NotificationType
                                .BACKLOG_ITEM_ADDED_TO_SPRINT,
                        "Backlog Item đã được thêm vào Sprint",
                        savedItem.getTitle()
                                + " đã được thêm vào "
                                + sprint.getName(),
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        recipientIds
                )
        );

        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    //====================== Đưa Item Khỏi Sprint =============================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public BacklogItemResponse removeBacklogItem(
            UUID projectId,
            UUID sprintId,
            UUID itemId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireSprintBacklogManagementAccess(
                        projectId,
                        currentUser
                );

        SprintValidator
                .validateProjectEditable(project);

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validatePlanning(
                sprint
        );

        BacklogItem backlogItem =
                backlogItemRepository
                        .findByIdAndProjectId(
                                itemId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BACKLOG_ITEM_NOT_FOUND
                                )
                        );

        if (backlogItem.getSprintId() == null
                || !backlogItem.getSprintId()
                .equals(sprintId)) {

            throw new BusinessException(
                    ErrorCode.BACKLOG_ITEM_NOT_IN_SPRINT
            );
        }

        Long oldPosition =
                backlogItem.getPosition();

        backlogItemRepository
                .shiftPositionsDown(
                        projectId,
                        sprintId,
                        oldPosition
                );

        Long maxBacklogPosition =
                backlogItemRepository
                        .findMaxBacklogPosition(
                                projectId
                        );

        long newPosition =
                maxBacklogPosition == null
                        ? 1L
                        : maxBacklogPosition + 1L;

        backlogItem.setSprintId(null);

        backlogItem.setStatus(
                BacklogItemStatus.READY
        );

        backlogItem.setPosition(
                newPosition
        );

        BacklogItem savedItem =
                backlogItemRepository.save(
                        backlogItem
                );

        taskSprintSyncService
                .detachBacklogItemTasksFromSprint(
                        projectId,
                        savedItem.getId(),
                        sprintId,
                        currentUser.getId()
                );

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "sprintId",
                sprintId
        );
        oldValue.put(
                "status",
                BacklogItemStatus.IN_SPRINT
        );
        oldValue.put(
                "position",
                oldPosition
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "sprintId",
                null
        );
        newValue.put(
                "status",
                BacklogItemStatus.READY
        );
        newValue.put(
                "position",
                newPosition
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        ProjectActivityAction
                                .BACKLOG_ITEM_REMOVED_FROM_SPRINT,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

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
                        NotificationType
                                .BACKLOG_ITEM_REMOVED_FROM_SPRINT,
                        "Backlog Item đã được lấy khỏi Sprint",
                        savedItem.getTitle()
                                + " đã được lấy khỏi "
                                + sprint.getName(),
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.BACKLOG_ITEM,
                        savedItem.getId(),
                        recipientIds
                )
        );

        return backlogItemMapper.toResponse(
                savedItem
        );
    }

    // ===================== Start Sprint =====================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
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

        SprintValidator
                .validateProjectEditable(
                        project
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validateCanStart(
                sprint
        );

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

        List<BacklogItem> backlogItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        if (backlogItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SPRINT_EMPTY
            );
        }

        boolean hasInvalidItem =
                backlogItems
                        .stream()
                        .anyMatch(item ->
                                item.getStatus()
                                        != BacklogItemStatus.IN_SPRINT
                        );

        if (hasInvalidItem) {
            throw new BusinessException(
                    ErrorCode
                            .BACKLOG_ITEM_STATUS_TRANSITION_INVALID
            );
        }

        Map<String, Object> oldValue =
                sprintSnapshot(
                        sprint
                );

        sprint.setStatus(
                SprintStatus.ACTIVE
        );

        sprint.setStartedAt(
                Instant.now()
        );

        sprint.setCompletedAt(null);

        Sprint savedSprint =
                sprintRepository.save(
                        sprint
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        ProjectActivityAction.SPRINT_STARTED,
                        currentUser.getId(),
                        oldValue,
                        sprintSnapshot(savedSprint)
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.SPRINT_STARTED,
                        "Sprint đã bắt đầu",
                        savedSprint.getName()
                                + " đã chính thức bắt đầu",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        getProjectRecipientIds(projectId)
                )
        );

        return toResponse(savedSprint);
    }

    // ===================== Complete Sprint =====================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
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

        SprintValidator
                .validateProjectEditable(
                        project
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validateCanComplete(
                sprint
        );

        List<BacklogItem> backlogItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        boolean hasUnfinishedItems =
                backlogItems
                        .stream()
                        .anyMatch(item ->
                                item.getStatus()
                                        != BacklogItemStatus.DONE
                        );

        if (hasUnfinishedItems) {
            throw new BusinessException(
                    ErrorCode.SPRINT_HAS_UNFINISHED_ITEMS
            );
        }

        Map<String, Object> oldValue =
                sprintSnapshot(
                        sprint
                );

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

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        ProjectActivityAction.SPRINT_COMPLETED,
                        currentUser.getId(),
                        oldValue,
                        sprintSnapshot(savedSprint)
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.SPRINT_COMPLETED,
                        "Sprint đã hoàn thành",
                        savedSprint.getName()
                                + " đã hoàn thành",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        getProjectRecipientIds(projectId)
                )
        );

        return toResponse(savedSprint);
    }

    // ===================== Cancel Sprint =====================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.SPRINT_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.BACKLOG_ITEM_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_BURNDOWN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CAPACITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_HEALTH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_RISKS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_PROGRESS,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_CLOSING_REPORT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
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

        SprintValidator
                .validateProjectEditable(
                        project
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        SprintValidator.validateCanCancel(
                sprint
        );

        List<BacklogItem> sprintItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        Map<String, Object> oldValue =
                sprintSnapshot(
                        sprint
                );

        Long currentMaxBacklogPosition =
                backlogItemRepository
                        .findMaxBacklogPosition(
                                projectId
                        );

        long nextBacklogPosition =
                currentMaxBacklogPosition == null
                        ? 1L
                        : currentMaxBacklogPosition + 1L;

        List<BacklogItem> itemsToReturn =
                new ArrayList<>();

        for (BacklogItem item : sprintItems) {

            /*
             * Backlog Item DONE giữ lại trong Sprint
             * để lưu lịch sử hoàn thành.
             */
            if (item.getStatus()
                    == BacklogItemStatus.DONE) {
                continue;
            }

            item.setSprintId(null);

            item.setStatus(
                    BacklogItemStatus.READY
            );

            item.setPosition(
                    nextBacklogPosition++
            );

            itemsToReturn.add(item);
        }

        if (!itemsToReturn.isEmpty()) {
            backlogItemRepository.saveAll(
                    itemsToReturn
            );
        }

        taskSprintSyncService
                .handleSprintCancellation(
                        projectId,
                        sprintId,
                        currentUser.getId()
                );
        sprint.setStatus(
                SprintStatus.CANCELLED
        );

        sprint.setCompletedAt(null);

        Sprint savedSprint =
                sprintRepository.save(
                        sprint
                );

        Map<String, Object> newValue =
                sprintSnapshot(
                        savedSprint
                );

        long returnedItemCount =
                itemsToReturn.size();

        newValue.put(
                "returnedItemCount",
                returnedItemCount
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        ProjectActivityAction.SPRINT_CANCELLED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.SPRINT_CANCELLED,
                        "Sprint đã bị hủy",
                        savedSprint.getName()
                                + " đã bị hủy",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.SPRINT,
                        savedSprint.getId(),
                        getProjectRecipientIds(projectId)
                )
        );

        return toResponse(savedSprint);
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

    private List<UUID> getProjectRecipientIds(
            UUID projectId
    ) {
        return projectMemberRepository
                .findAllByProjectIdOrderByJoinedAtAsc(
                        projectId
                )
                .stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .toList();
    }

    private Map<String, Object> sprintSnapshot(
            Sprint sprint
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "name",
                sprint.getName()
        );
        value.put(
                "goal",
                sprint.getGoal()
        );
        value.put(
                "status",
                sprint.getStatus()
        );
        value.put(
                "startDate",
                sprint.getStartDate()
        );
        value.put(
                "endDate",
                sprint.getEndDate()
        );
        value.put(
                "startedAt",
                sprint.getStartedAt()
        );
        value.put(
                "completedAt",
                sprint.getCompletedAt()
        );

        return value;
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
