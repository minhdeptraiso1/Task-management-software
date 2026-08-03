package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.bug.AssignBugRequest;
import com.project.taskmanagement.dto.request.bug.BugSearchRequest;
import com.project.taskmanagement.dto.request.bug.CreateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugPriorityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugSeverityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugStatusRequest;
import com.project.taskmanagement.dto.response.bug.BugPageResponse;
import com.project.taskmanagement.dto.response.bug.BugResponse;
import com.project.taskmanagement.dto.response.bug.BugSummaryResponse;
import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.spec.BugSpecification;
import com.project.taskmanagement.service.BugService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.bug.BugViewHelper;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.DateRangeValidator;
import com.project.taskmanagement.service.validation.BugStatusTransitionValidator;
import com.project.taskmanagement.service.validation.BugValidator;
import com.project.taskmanagement.service.validation.PageableValidator;
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
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class BugServiceImpl implements BugService {

    BugRepository bugRepository;
    TaskRepository taskRepository;
    BacklogItemRepository backlogItemRepository;
    SprintRepository sprintRepository;
    ProjectMemberRepository projectMemberRepository;
    UserLookupHelper userLookupHelper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    NotificationService notificationService;
    BugViewHelper bugViewHelper;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse create(
            UUID projectId,
            CreateBugRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireBugCreateAccess(projectId, currentUser);
        validateLinkedEntities(projectId, request.taskId(), request.backlogItemId(), request.sprintId());

        if (request.assigneeUserId() != null) {
            validateAssignee(projectId, request.assigneeUserId());
        }

        Bug bug =
                Bug.builder()
                        .projectId(projectId)
                        .backlogItemId(request.backlogItemId())
                        .taskId(request.taskId())
                        .sprintId(request.sprintId())
                        .title(normalizeRequired(request.title()))
                        .description(normalize(request.description()))
                        .severity(request.severity())
                        .priority(request.priority())
                        .status(BugStatus.OPEN)
                        .assigneeUserId(request.assigneeUserId())
                        .reporterUserId(currentUser.getId())
                        .reproductionSteps(normalize(request.reproductionSteps()))
                        .expectedResult(normalize(request.expectedResult()))
                        .actualResult(normalize(request.actualResult()))
                        .dueDate(request.dueDate())
                        .build();

        Bug savedBug =
                bugRepository.save(bug);

        logActivity(
                projectId,
                savedBug.getId(),
                ProjectActivityAction.BUG_CREATED,
                currentUser.getId(),
                null,
                snapshot(savedBug)
        );

        sendBugAssignedNotification(savedBug, currentUser.getId());

        if (savedBug.getSeverity() == BugSeverity.CRITICAL) {
            sendCriticalBugNotification(savedBug, currentUser.getId());
        }

        return toResponse(savedBug);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.BUG_SEARCH,
            key = "{#projectId,#request,#pageable.pageNumber,#pageable.pageSize,#pageable.sort.toString()}"
    )
    public BugPageResponse search(
            UUID projectId,
            BugSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(project, currentUser);

        BugSearchRequest safeRequest =
                request == null
                        ? new BugSearchRequest(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
                        : request;

        DateRangeValidator.validate(
                safeRequest.dueDateFrom(),
                safeRequest.dueDateTo()
        );
        DateRangeValidator.validate(
                safeRequest.createdFrom(),
                safeRequest.createdTo()
        );
        PageableValidator.validate(
                pageable,
                Set.of(
                        "createdAt",
                        "updatedAt",
                        "title",
                        "status",
                        "priority",
                        "severity",
                        "dueDate",
                        "reopenedCount"
                )
        );

        LocalDate today = LocalDate.now();

        Specification<Bug> specification =
                Specification.where(BugSpecification.belongsToProject(projectId))
                        .and(BugSpecification.search(safeRequest.keyword()))
                        .and(BugSpecification.hasStatus(safeRequest.status()))
                        .and(BugSpecification.hasSeverity(safeRequest.severity()))
                        .and(BugSpecification.hasPriority(safeRequest.priority()))
                        .and(BugSpecification.hasAssignee(safeRequest.assigneeUserId()))
                        .and(BugSpecification.hasReporter(safeRequest.reporterUserId()))
                        .and(BugSpecification.hasTask(safeRequest.taskId()))
                        .and(BugSpecification.hasLinkedTask(safeRequest.linkedTaskId()))
                        .and(BugSpecification.hasBacklogItem(safeRequest.backlogItemId()))
                        .and(BugSpecification.hasSprint(safeRequest.sprintId()))
                        .and(BugSpecification.reopenedOnly(safeRequest.reopenedOnly()))
                        .and(BugSpecification.overdueOnly(safeRequest.overdueOnly(), today))
                        .and(BugSpecification.dueDateBetween(
                                safeRequest.dueDateFrom(),
                                safeRequest.dueDateTo()
                        ))
                        .and(BugSpecification.createdAtBetween(
                                safeRequest.createdFrom(),
                                safeRequest.createdTo()
                        ));

        Page<Bug> bugPage = bugRepository.findAll(specification, pageable);
        LinkedHashSet<UUID> userIds = new LinkedHashSet<>();
        bugPage.getContent().forEach(bug -> {
            userIds.add(bug.getAssigneeUserId());
            userIds.add(bug.getReporterUserId());
        });
        Map<UUID, User> usersById = userLookupHelper.findUserMap(userIds);
        Page<BugResponse> page = bugPage.map(bug -> toResponse(bug, usersById));

        return BugPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}")
    public BugResponse getById(
            UUID projectId,
            UUID bugId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(project, currentUser);

        return toResponse(
                getBugOrThrow(projectId, bugId)
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse update(
            UUID projectId,
            UUID bugId,
            UpdateBugRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Bug bug =
                getBugOrThrow(projectId, bugId);

        requireBugUpdateAccess(projectId, bug, currentUser);
        BugValidator.validateEditable(bug);

        validateLinkedEntities(projectId, request.taskId(), request.backlogItemId(), request.sprintId());

        Map<String, Object> oldValue =
                snapshot(bug);

        if (request.backlogItemId() != null) {
            bug.setBacklogItemId(request.backlogItemId());
        }
        if (request.taskId() != null) {
            bug.setTaskId(request.taskId());
        }
        if (request.sprintId() != null) {
            bug.setSprintId(request.sprintId());
        }
        if (request.title() != null) {
            bug.setTitle(normalizeRequired(request.title()));
        }
        if (request.description() != null) {
            bug.setDescription(normalize(request.description()));
        }
        if (request.severity() != null) {
            bug.setSeverity(request.severity());
        }
        if (request.priority() != null) {
            bug.setPriority(request.priority());
        }
        if (request.reproductionSteps() != null) {
            bug.setReproductionSteps(normalize(request.reproductionSteps()));
        }
        if (request.expectedResult() != null) {
            bug.setExpectedResult(normalize(request.expectedResult()));
        }
        if (request.actualResult() != null) {
            bug.setActualResult(normalize(request.actualResult()));
        }
        if (request.dueDate() != null) {
            bug.setDueDate(request.dueDate());
        }

        Bug savedBug =
                bugRepository.save(bug);

        logActivity(
                projectId,
                savedBug.getId(),
                ProjectActivityAction.BUG_UPDATED,
                currentUser.getId(),
                oldValue,
                snapshot(savedBug)
        );

        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse assign(
            UUID projectId,
            UUID bugId,
            AssignBugRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireBugAssignAccess(projectId, currentUser);
        validateAssignee(projectId, request.assigneeUserId());

        Bug bug =
                getBugOrThrow(projectId, bugId);
        BugValidator.validateEditable(bug);

        UUID oldAssigneeUserId =
                bug.getAssigneeUserId();

        if (request.assigneeUserId().equals(oldAssigneeUserId)) {
            return toResponse(bug);
        }

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put("assigneeUserId", oldAssigneeUserId);

        bug.setAssigneeUserId(request.assigneeUserId());
        if (bug.getStatus() == BugStatus.OPEN) {
            bug.setStatus(BugStatus.ASSIGNED);
        }

        Bug savedBug =
                bugRepository.save(bug);

        Map<String, Object> newValue =
                Map.of("assigneeUserId", savedBug.getAssigneeUserId());

        logActivity(
                projectId,
                savedBug.getId(),
                ProjectActivityAction.BUG_ASSIGNED,
                currentUser.getId(),
                oldValue,
                newValue
        );

        sendBugAssignedNotification(savedBug, currentUser.getId());

        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse unassign(UUID projectId, UUID bugId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        requireBugAssignAccess(projectId, currentUser);

        Bug bug = getBugOrThrow(projectId, bugId);
        BugValidator.validateEditable(bug);
        UUID oldAssigneeUserId = bug.getAssigneeUserId();
        if (oldAssigneeUserId == null) {
            return toResponse(bug);
        }

        bug.setAssigneeUserId(null);
        if (bug.getStatus() == BugStatus.ASSIGNED) {
            bug.setStatus(BugStatus.OPEN);
        }
        Bug savedBug = bugRepository.save(bug);

        Map<String, Object> oldValue = Map.of("assigneeUserId", oldAssigneeUserId);
        Map<String, Object> newValue = new LinkedHashMap<>();
        newValue.put("assigneeUserId", null);
        logActivity(projectId, savedBug.getId(), ProjectActivityAction.BUG_UNASSIGNED,
                currentUser.getId(), oldValue, newValue);
        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse updateStatus(
            UUID projectId,
            UUID bugId,
            UpdateBugStatusRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Bug bug =
                getBugOrThrow(projectId, bugId);

        BugStatus oldStatus =
                bug.getStatus();

        BugStatus newStatus =
                request.status();

        requireBugStatusAccess(projectId, bug, currentUser, newStatus);
        BugStatusTransitionValidator.validate(oldStatus, newStatus);

        if (oldStatus == newStatus) {
            return toResponse(bug);
        }

        Map<String, Object> oldValue =
                Map.of("status", oldStatus);

        bug.setStatus(newStatus);

        if (newStatus == BugStatus.RESOLVED) {
            bug.setResolvedAt(Instant.now());
        }

        if (newStatus == BugStatus.CLOSED) {
            bug.setClosedAt(Instant.now());
        }

        if (newStatus == BugStatus.VERIFIED) {
            bug.setClosedAt(null);
        }

        if (newStatus == BugStatus.REOPENED) {
            bug.setResolvedAt(null);
            bug.setClosedAt(null);
            if (oldStatus == BugStatus.RESOLVED
                    || oldStatus == BugStatus.VERIFIED
                    || oldStatus == BugStatus.CLOSED) {
                bug.setReopenedCount(
                        bug.getReopenedCount() == null
                                ? 1
                                : bug.getReopenedCount() + 1
                );
            }
        }

        Bug savedBug =
                bugRepository.save(bug);

        Map<String, Object> newValue =
                Map.of("status", newStatus);

        logActivity(
                projectId,
                savedBug.getId(),
                ProjectActivityAction.BUG_STATUS_CHANGED,
                currentUser.getId(),
                oldValue,
                newValue
        );

        sendBugStatusNotification(
                savedBug,
                currentUser.getId(),
                oldStatus,
                newStatus
        );

        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse updateSeverity(UUID projectId, UUID bugId, UpdateBugSeverityRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = getBugOrThrow(projectId, bugId);
        requireBugUpdateAccess(projectId, bug, currentUser);
        BugValidator.validateEditable(bug);

        Map<String, Object> oldValue = Map.of("severity", bug.getSeverity());
        bug.setSeverity(request.severity());
        Bug savedBug = bugRepository.save(bug);
        Map<String, Object> newValue = Map.of("severity", savedBug.getSeverity());
        logActivity(projectId, savedBug.getId(), ProjectActivityAction.BUG_SEVERITY_CHANGED,
                currentUser.getId(), oldValue, newValue);
        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public BugResponse updatePriority(UUID projectId, UUID bugId, UpdateBugPriorityRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Bug bug = getBugOrThrow(projectId, bugId);
        requireBugUpdateAccess(projectId, bug, currentUser);
        BugValidator.validateEditable(bug);

        Map<String, Object> oldValue = Map.of("priority", bug.getPriority());
        bug.setPriority(request.priority());
        Bug savedBug = bugRepository.save(bug);
        Map<String, Object> newValue = Map.of("priority", savedBug.getPriority());
        logActivity(projectId, savedBug.getId(), ProjectActivityAction.BUG_PRIORITY_CHANGED,
                currentUser.getId(), oldValue, newValue);
        return toResponse(savedBug);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUG_DETAIL, key = "{#projectId,#bugId}"),
            @CacheEvict(cacheNames = CacheNames.BUG_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId"),
            @CacheEvict(cacheNames = CacheNames.BUG_REPORT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUG_QA_METRICS, allEntries = true)
    })
    public void delete(
            UUID projectId,
            UUID bugId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        requireBugDeleteAccess(projectId, currentUser);

        Bug bug =
                getBugOrThrow(projectId, bugId);
        BugValidator.validateEditable(bug);

        Map<String, Object> oldValue =
                snapshot(bug);

        bug.markDeleted(currentUser.getUsername());
        bugRepository.save(bug);

        logActivity(
                projectId,
                bugId,
                ProjectActivityAction.BUG_DELETED,
                currentUser.getId(),
                oldValue,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_SUMMARY, key = "#projectId")
    public BugSummaryResponse getSummary(
            UUID projectId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(project, currentUser);

        Map<String, Long> byStatus =
                new LinkedHashMap<>();

        for (BugStatus status : BugStatus.values()) {
            byStatus.put(
                    status.name(),
                    bugRepository.countByProjectIdAndStatus(projectId, status)
            );
        }

        Map<String, Long> bySeverity =
                new LinkedHashMap<>();

        for (BugSeverity severity : BugSeverity.values()) {
            bySeverity.put(
                    severity.name(),
                    bugRepository.countByProjectIdAndSeverity(projectId, severity)
            );
        }

        return new BugSummaryResponse(
                projectId,
                bugRepository.countByProjectId(projectId),
                byStatus.get(BugStatus.OPEN.name()),
                byStatus.get(BugStatus.ASSIGNED.name()),
                byStatus.get(BugStatus.IN_PROGRESS.name()),
                byStatus.get(BugStatus.RESOLVED.name()),
                byStatus.get(BugStatus.VERIFIED.name()),
                byStatus.get(BugStatus.REOPENED.name()),
                byStatus.get(BugStatus.CLOSED.name()),
                byStatus.get(BugStatus.CANCELLED.name()),
                bySeverity.get(BugSeverity.CRITICAL.name()),
                bySeverity.get(BugSeverity.HIGH.name()),
                bugRepository.countByProjectIdAndAssigneeUserIdIsNotNull(projectId),
                bugRepository.countByProjectIdAndAssigneeUserIdIsNull(projectId),
                byStatus,
                bySeverity
        );
    }

    private Bug getBugOrThrow(UUID projectId, UUID bugId) {
        return bugRepository
                .findByIdAndProjectId(bugId, projectId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.BUG_NOT_FOUND)
                );
    }

    private void validateLinkedEntities(
            UUID projectId,
            UUID taskId,
            UUID backlogItemId,
            UUID sprintId
    ) {
        if (taskId != null
                && taskRepository
                .findByIdAndProjectId(taskId, projectId)
                .isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_TASK_NOT_IN_PROJECT);
        }

        if (backlogItemId != null
                && backlogItemRepository
                .findByIdAndProjectId(backlogItemId, projectId)
                .isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_BACKLOG_ITEM_NOT_IN_PROJECT);
        }

        if (sprintId != null
                && sprintRepository
                .findByIdAndProjectId(sprintId, projectId)
                .isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_SPRINT_NOT_IN_PROJECT);
        }
    }

    private void validateAssignee(
            UUID projectId,
            UUID assigneeUserId
    ) {
        if (projectMemberRepository
                .findByProjectIdAndUserId(projectId, assigneeUserId)
                .isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_ASSIGNEE_NOT_PROJECT_MEMBER);
        }
    }

    private void requireBugCreateAccess(
            UUID projectId,
            User currentUser
    ) {
        ProjectMember member =
                projectAccessService.getMembershipOrThrow(projectId, currentUser.getId());

        ProjectMemberRole role =
                member.getRole();

        if (role == ProjectMemberRole.VIEWER) {
            throw new BusinessException(ErrorCode.BUG_ACCESS_DENIED);
        }
    }

    private void requireBugUpdateAccess(
            UUID projectId,
            Bug bug,
            User currentUser
    ) {
        ProjectMemberRole role =
                projectAccessService
                        .getMembershipOrThrow(projectId, currentUser.getId())
                        .getRole();

        if (role != ProjectMemberRole.OWNER
                && role != ProjectMemberRole.PROJECT_MANAGER
                && role != ProjectMemberRole.SCRUM_MASTER
                && role != ProjectMemberRole.PRODUCT_OWNER
                && role != ProjectMemberRole.TESTER
                && !currentUser.getId().equals(bug.getReporterUserId())) {
            throw new BusinessException(ErrorCode.BUG_ACCESS_DENIED);
        }
    }

    private void requireBugAssignAccess(
            UUID projectId,
            User currentUser
    ) {
        ProjectMemberRole role =
                projectAccessService
                        .getMembershipOrThrow(projectId, currentUser.getId())
                        .getRole();

        if (role != ProjectMemberRole.OWNER
                && role != ProjectMemberRole.PROJECT_MANAGER
                && role != ProjectMemberRole.SCRUM_MASTER) {
            throw new BusinessException(ErrorCode.BUG_ACCESS_DENIED);
        }
    }

    private void requireBugDeleteAccess(
            UUID projectId,
            User currentUser
    ) {
        requireBugAssignAccess(projectId, currentUser);
    }

    private void requireBugStatusAccess(
            UUID projectId,
            Bug bug,
            User currentUser,
            BugStatus newStatus
    ) {
        ProjectMemberRole role =
                projectAccessService
                        .getMembershipOrThrow(projectId, currentUser.getId())
                        .getRole();

        if (role == ProjectMemberRole.OWNER
                || role == ProjectMemberRole.PROJECT_MANAGER
                || role == ProjectMemberRole.SCRUM_MASTER) {
            return;
        }

        if (bug.getAssigneeUserId() != null
                && bug.getAssigneeUserId().equals(currentUser.getId())
                && (newStatus == BugStatus.IN_PROGRESS
                || newStatus == BugStatus.RESOLVED)) {
            return;
        }

        if ((role == ProjectMemberRole.TESTER
                || currentUser.getId().equals(bug.getReporterUserId()))
                && (newStatus == BugStatus.VERIFIED
                || newStatus == BugStatus.REOPENED
                || newStatus == BugStatus.CLOSED)) {
            return;
        }

        throw new BusinessException(ErrorCode.BUG_ACCESS_DENIED);
    }

    private void logActivity(
            UUID projectId,
            UUID bugId,
            ProjectActivityAction action,
            UUID actorUserId,
            Map<String, Object> oldValue,
            Map<String, Object> newValue
    ) {
        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.BUG,
                        bugId,
                        action,
                        actorUserId,
                        oldValue,
                        newValue
                )
        );
    }

    private Map<String, Object> snapshot(Bug bug) {
        Map<String, Object> map =
                new LinkedHashMap<>();

        map.put("id", bug.getId());
        map.put("projectId", bug.getProjectId());
        map.put("backlogItemId", bug.getBacklogItemId());
        map.put("taskId", bug.getTaskId());
        map.put("sprintId", bug.getSprintId());
        map.put("title", bug.getTitle());
        map.put("severity", bug.getSeverity());
        map.put("priority", bug.getPriority());
        map.put("status", bug.getStatus());
        map.put("assigneeUserId", bug.getAssigneeUserId());
        map.put("reporterUserId", bug.getReporterUserId());
        map.put("dueDate", bug.getDueDate());
        map.put("reopenedCount", bug.getReopenedCount());

        return map;
    }

    private void sendBugAssignedNotification(
            Bug bug,
            UUID actorUserId
    ) {
        if (bug.getAssigneeUserId() == null
                || bug.getAssigneeUserId().equals(actorUserId)) {
            return;
        }

        notificationService.create(
                new NotificationCommand(
                        NotificationType.BUG_ASSIGNED,
                        "Bạn được gán xử lý Bug",
                        bug.getTitle(),
                        actorUserId,
                        bug.getProjectId(),
                        ActivityEntityType.BUG,
                        bug.getId(),
                        List.of(bug.getAssigneeUserId())
                )
        );
    }

    private void sendBugStatusNotification(
            Bug bug,
            UUID actorUserId,
            BugStatus oldStatus,
            BugStatus newStatus
    ) {
        Set<UUID> recipients = new LinkedHashSet<>();
        recipients.add(bug.getReporterUserId());
        recipients.add(bug.getAssigneeUserId());
        recipients.remove(null);
        recipients.remove(actorUserId);
        if (recipients.isEmpty()) {
            return;
        }

        NotificationType type =
                newStatus == BugStatus.REOPENED
                        ? NotificationType.BUG_REOPENED
                        : NotificationType.BUG_STATUS_CHANGED;

        notificationService.create(
                new NotificationCommand(
                        type,
                        "Bug đã đổi trạng thái",
                        oldStatus + " -> " + newStatus,
                        actorUserId,
                        bug.getProjectId(),
                        ActivityEntityType.BUG,
                        bug.getId(),
                        recipients
                )
        );
    }

    private void sendCriticalBugNotification(
            Bug bug,
            UUID actorUserId
    ) {
        if (bug.getReporterUserId() == null
                || bug.getReporterUserId().equals(actorUserId)) {
            return;
        }

        notificationService.create(
                new NotificationCommand(
                        NotificationType.BUG_CRITICAL,
                        "Bug nghiêm trọng được tạo",
                        bug.getTitle(),
                        actorUserId,
                        bug.getProjectId(),
                        ActivityEntityType.BUG,
                        bug.getId(),
                        List.of(bug.getReporterUserId())
                )
        );
    }

    private BugResponse toResponse(Bug bug) {
        LinkedHashSet<UUID> userIds = new LinkedHashSet<>();
        userIds.add(bug.getAssigneeUserId());
        userIds.add(bug.getReporterUserId());
        return toResponse(bug, userLookupHelper.findUserMap(userIds));
    }

    private BugResponse toResponse(Bug bug, Map<UUID, User> usersById) {
        User assignee = userLookupHelper.getOrNull(usersById, bug.getAssigneeUserId());
        User reporter = userLookupHelper.getOrNull(usersById, bug.getReporterUserId());

        return new BugResponse(
                bug.getId(),
                bug.getProjectId(),
                bug.getBacklogItemId(),
                bug.getTaskId(),
                bug.getSprintId(),
                bug.getTitle(),
                bug.getDescription(),
                bug.getSeverity(),
                bug.getPriority(),
                bug.getStatus(),
                bug.getAssigneeUserId(),
                assignee == null ? null : assignee.getUsername(),
                assignee == null ? null : assignee.getEmail(),
                bug.getReporterUserId(),
                reporter == null ? null : reporter.getUsername(),
                reporter == null ? null : reporter.getEmail(),
                bug.getReproductionSteps(),
                bug.getExpectedResult(),
                bug.getActualResult(),
                bug.getDueDate(),
                bug.getReopenedCount(),
                bugViewHelper.isOverdue(bug),
                bug.getResolvedAt(),
                bug.getClosedAt(),
                bugViewHelper.targetUrl(bug),
                bug.getCreatedAt(),
                bug.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return TextNormalizer.trimToNull(value);
    }

    private String normalizeRequired(String value) {
        String normalized =
                TextNormalizer.trim(value);

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        return normalized;
    }
}
