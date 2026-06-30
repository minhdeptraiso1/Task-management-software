package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.task.*;
import com.project.taskmanagement.dto.response.kanban.KanbanBoardResponse;
import com.project.taskmanagement.dto.response.kanban.KanbanColumnResponse;
import com.project.taskmanagement.dto.response.kanban.KanbanSummaryResponse;
import com.project.taskmanagement.dto.response.kanban.KanbanTaskResponse;
import com.project.taskmanagement.dto.response.task.TaskPageResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.repository.spec.TaskSpecification;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.TaskValidator;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskServiceImpl
        implements TaskService {

    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;

    BacklogItemRepository backlogItemRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityService projectActivityService;
    NotificationService notificationService;

    SprintRepository sprintRepository;

    // ===================== CREATE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
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
            )
    })
    public TaskResponse create(
            UUID projectId,
            CreateTaskRequest request
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
                .requireTaskManagementAccess(
                        projectId,
                        currentUser
                );

        TaskValidator.validateProjectEditable(
                project
        );

        TaskValidator.validateDates(
                request.startDate(),
                request.dueDate()
        );

        BacklogItem backlogItem =
                backlogItemRepository
                        .findByIdAndProjectId(
                                request.backlogItemId(),
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BACKLOG_ITEM_NOT_FOUND
                                )
                        );

        UUID assigneeUserId =
                request.assigneeUserId();

        if (assigneeUserId != null) {
            validateAssignee(
                    projectId,
                    assigneeUserId
            );
        }

        TaskStatus initialStatus =
                TaskStatus.TODO;

        Long maxPosition =
                taskRepository.findMaxPosition(
                        projectId,
                        backlogItem.getSprintId(),
                        initialStatus
                );

        long nextPosition =
                maxPosition == null
                        ? 1L
                        : maxPosition + 1L;

        TaskType type =
                request.type() != null
                        ? request.type()
                        : TaskType.DEVELOPMENT;

        TaskPriority priority =
                request.priority() != null
                        ? request.priority()
                        : TaskPriority.MEDIUM;

        Task task =
                Task.builder()
                        .projectId(projectId)
                        .backlogItemId(
                                backlogItem.getId()
                        )
                        .currentSprintId(
                                backlogItem.getSprintId()
                        )
                        .originSprintId(
                                backlogItem.getSprintId()
                        )
                        .title(
                                TextNormalizer.trim(
                                        request.title()
                                )
                        )
                        .description(
                                TextNormalizer.trimToNull(
                                        request.description()
                                )
                        )
                        .type(type)
                        .status(initialStatus)
                        .priority(priority)
                        .assigneeUserId(
                                assigneeUserId
                        )
                        .reporterUserId(
                                currentUser.getId()
                        )
                        .estimatedMinutes(
                                request.estimatedMinutes()
                        )
                        .startDate(
                                request.startDate()
                        )
                        .dueDate(
                                request.dueDate()
                        )
                        .completedAt(null)
                        .position(nextPosition)
                        .build();

        Task savedTask =
                taskRepository.save(task);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction.TASK_CREATED,
                        currentUser.getId(),
                        null,
                        taskSnapshot(savedTask)
                )
        );

        if (assigneeUserId != null) {
            sendAssignmentNotification(
                    projectId,
                    savedTask,
                    currentUser.getId(),
                    assigneeUserId
            );
        }

        return toResponse(savedTask);
    }

    // ===================== SEARCH =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.TASK_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + (#request == null || #request.sprintId() == null ? '' : #request.sprintId())" +
                    " + '|backlog=' + (#request == null || #request.backlogItemId() == null ? '' : #request.backlogItemId())" +
                    " + '|assignee=' + (#request == null || #request.assigneeUserId() == null ? '' : #request.assigneeUserId())" +
                    " + '|status=' + (#request == null || #request.status() == null ? '' : #request.status())" +
                    " + '|priority=' + (#request == null || #request.priority() == null ? '' : #request.priority())" +
                    " + '|keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword())" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public TaskPageResponse search(
            UUID projectId,
            TaskSearchRequest request,
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

        Specification<Task> specification =
                Specification.allOf(
                        TaskSpecification
                                .belongsToProject(projectId),

                        TaskSpecification
                                .search(
                                        request != null
                                                ? request.keyword()
                                                : null
                                ),

                        TaskSpecification
                                .hasBacklogItemId(
                                        request != null
                                                ? request.backlogItemId()
                                                : null
                                ),

                        TaskSpecification
                                .hasSprintId(
                                        request != null
                                                ? request.sprintId()
                                                : null
                                ),

                        TaskSpecification
                                .hasAssignee(
                                        request != null
                                                ? request.assigneeUserId()
                                                : null
                                ),

                        TaskSpecification
                                .hasStatus(
                                        request != null
                                                ? request.status()
                                                : null
                                ),

                        TaskSpecification
                                .hasPriority(
                                        request != null
                                                ? request.priority()
                                                : null
                                ),

                        TaskSpecification
                                .hasType(
                                        request != null
                                                ? request.type()
                                                : null
                                ),

                        TaskSpecification
                                .unassignedOnly(
                                        request != null
                                                ? request.unassignedOnly()
                                                : null
                                )
                );

        Page<TaskResponse> responsePage =
                taskRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toResponse);

        return TaskPageResponse.from(
                responsePage
        );
    }

    // ===================== DETAIL =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.TASK_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #taskId"
    )
    public TaskResponse getById(
            UUID projectId,
            UUID taskId
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

        return toResponse(
                getTaskOrThrow(
                        projectId,
                        taskId
                )
        );
    }

    // ===================== UPDATE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
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
                    value = CacheNames.TASK_TIME_SUMMARY,
                    allEntries = true
            )
    })
    public TaskResponse update(
            UUID projectId,
            UUID taskId,
            UpdateTaskRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireTaskManagementAccess(
                        projectId,
                        currentUser
                );

        TaskValidator.validateProjectEditable(
                project
        );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        TaskValidator.validateEditable(task);

        Map<String, Object> oldValue =
                taskSnapshot(task);

        var newStartDate =
                request.startDate() != null
                        ? request.startDate()
                        : task.getStartDate();

        var newDueDate =
                request.dueDate() != null
                        ? request.dueDate()
                        : task.getDueDate();

        TaskValidator.validateDates(
                newStartDate,
                newDueDate
        );

        if (request.title() != null) {
            String title =
                    TextNormalizer.trim(
                            request.title()
                    );

            if (title.isBlank()) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR
                );
            }

            task.setTitle(title);
        }

        if (request.description() != null) {
            task.setDescription(
                    TextNormalizer.trimToNull(
                            request.description()
                    )
            );
        }

        if (request.type() != null) {
            task.setType(
                    request.type()
            );
        }

        if (request.priority() != null) {
            task.setPriority(
                    request.priority()
            );
        }

        if (request.estimatedMinutes() != null) {
            task.setEstimatedMinutes(
                    request.estimatedMinutes()
            );
        }

        if (request.startDate() != null) {
            task.setStartDate(
                    request.startDate()
            );
        }

        if (request.dueDate() != null) {
            task.setDueDate(
                    request.dueDate()
            );
        }

        Task savedTask =
                taskRepository.save(task);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction.TASK_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        taskSnapshot(savedTask)
                )
        );

        return toResponse(savedTask);
    }

    // ===================== ASSIGN =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            )
    })
    public TaskResponse assign(
            UUID projectId,
            UUID taskId,
            AssignTaskRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireTaskAssignmentAccess(
                        projectId,
                        currentUser
                );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        UUID oldAssigneeId =
                task.getAssigneeUserId();

        UUID newAssigneeId =
                request.assigneeUserId();

        validateAssignee(
                projectId,
                newAssigneeId
        );

        if (newAssigneeId.equals(
                oldAssigneeId
        )) {
            return toResponse(task);
        }

        task.setAssigneeUserId(
                newAssigneeId
        );

        Task savedTask =
                taskRepository.save(task);

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "assigneeUserId",
                oldAssigneeId
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "assigneeUserId",
                newAssigneeId
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction.TASK_ASSIGNED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        sendAssignmentNotification(
                projectId,
                savedTask,
                currentUser.getId(),
                newAssigneeId
        );

        return toResponse(savedTask);
    }

    // ===================== UNASSIGN =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_KANBAN,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.SPRINT_TASK_STATISTICS,
                    allEntries = true
            )
    })
    public TaskResponse unassign(
            UUID projectId,
            UUID taskId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireTaskAssignmentAccess(
                        projectId,
                        currentUser
                );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        UUID oldAssigneeId =
                task.getAssigneeUserId();

        if (oldAssigneeId == null) {
            throw new BusinessException(
                    ErrorCode.TASK_ALREADY_UNASSIGNED
            );
        }

        task.setAssigneeUserId(null);

        Task savedTask =
                taskRepository.save(task);

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "assigneeUserId",
                oldAssigneeId
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "assigneeUserId",
                null
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction.TASK_UNASSIGNED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.TASK_UNASSIGNED,
                        "Bạn đã được bỏ phân công khỏi Task",
                        "Bạn không còn phụ trách Task "
                                + savedTask.getTitle(),
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        List.of(oldAssigneeId)
                )
        );

        return toResponse(savedTask);
    }

    // ===================== DELETE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
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
                    value = CacheNames.TASK_TIME_SUMMARY,
                    allEntries = true
            )
    })
    public void delete(
            UUID projectId,
            UUID taskId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireTaskAssignmentAccess(
                        projectId,
                        currentUser
                );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        Map<String, Object> oldValue =
                taskSnapshot(task);

        task.markDeleted(
                currentUser.getUsername()
        );

        taskRepository.save(task);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        taskId,
                        ProjectActivityAction.TASK_DELETED,
                        currentUser.getId(),
                        oldValue,
                        null
                )
        );
    }

    // ===================== Lấy Kanban Board =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_KANBAN,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #sprintId"
    )
    public KanbanBoardResponse getKanbanBoard(
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
                .requireViewAccess(
                        project,
                        currentUser
                );

        Sprint sprint =
                sprintRepository
                        .findByIdAndProjectId(
                                sprintId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.KANBAN_SPRINT_NOT_FOUND
                                )
                        );

        List<Task> sprintTasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintIdOrderByStatusAscPositionAsc(
                                projectId,
                                sprintId
                        );

        EnumMap<TaskStatus, List<KanbanTaskResponse>>
                tasksByStatus =
                new EnumMap<>(TaskStatus.class);

        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(
                    status,
                    new ArrayList<>()
            );
        }

        for (Task task : sprintTasks) {
            tasksByStatus
                    .get(task.getStatus())
                    .add(
                            toKanbanResponse(task)
                    );
        }

        for (List<KanbanTaskResponse> tasks :
                tasksByStatus.values()) {

            tasks.sort(
                    Comparator.comparing(
                            KanbanTaskResponse::position
                    )
            );
        }

        List<KanbanColumnResponse> columns =
                List.of(
                        createColumn(
                                TaskStatus.TODO,
                                "Cần làm",
                                tasksByStatus
                        ),
                        createColumn(
                                TaskStatus.IN_PROGRESS,
                                "Đang thực hiện",
                                tasksByStatus
                        ),
                        createColumn(
                                TaskStatus.IN_REVIEW,
                                "Đang kiểm tra",
                                tasksByStatus
                        ),
                        createColumn(
                                TaskStatus.BLOCKED,
                                "Bị chặn",
                                tasksByStatus
                        ),
                        createColumn(
                                TaskStatus.DONE,
                                "Hoàn thành",
                                tasksByStatus
                        )
                );

        KanbanSummaryResponse summary =
                new KanbanSummaryResponse(
                        sprintTasks.size(),
                        tasksByStatus
                                .get(TaskStatus.TODO)
                                .size(),
                        tasksByStatus
                                .get(TaskStatus.IN_PROGRESS)
                                .size(),
                        tasksByStatus
                                .get(TaskStatus.IN_REVIEW)
                                .size(),
                        tasksByStatus
                                .get(TaskStatus.DONE)
                                .size(),
                        tasksByStatus
                                .get(TaskStatus.BLOCKED)
                                .size(),
                        tasksByStatus
                                .get(TaskStatus.CANCELLED)
                                .size()
                );

        return new KanbanBoardResponse(
                projectId,
                sprint.getId(),
                sprint.getName(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                summary,
                columns
        );
    }

    // ===================== Đổi trạng thái =====================
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.TASK_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.TASK_SEARCH,
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
            )
    })
    public TaskResponse updateStatus(
            UUID projectId,
            UUID taskId,
            UpdateTaskStatusRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        TaskValidator.validateProjectEditable(
                project
        );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        projectAccessService
                .requireTaskStatusUpdateAccess(
                        projectId,
                        currentUser,
                        task
                );

        UUID sprintId =
                task.getCurrentSprintId();

        if (sprintId == null) {
            throw new BusinessException(
                    ErrorCode.TASK_NOT_IN_ACTIVE_SPRINT
            );
        }

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

        TaskValidator.validateSprintActive(
                sprint
        );

        TaskStatus oldStatus =
                task.getStatus();

        TaskStatus newStatus =
                request.status();

        if (oldStatus == newStatus) {
            if (request.position() == null) {
                return toResponse(task);
            }

            return moveInsideColumn(
                    projectId,
                    task,
                    request.position(),
                    currentUser.getId()
            );
        }

        TaskValidator.validateStatusTransition(
                oldStatus,
                newStatus
        );

        Long oldPosition =
                task.getPosition();

        taskRepository.shiftPositionsDown(
                projectId,
                sprintId,
                oldStatus,
                oldPosition
        );

        Long requestedPosition =
                request.position();

        long newColumnCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintIdAndStatus(
                                projectId,
                                sprintId,
                                newStatus
                        );

        long newPosition;

        if (requestedPosition == null) {
            newPosition = newColumnCount + 1L;
        } else {
            newPosition = Math.min(
                    requestedPosition,
                    newColumnCount + 1L
            );

            taskRepository.shiftPositionsUp(
                    projectId,
                    sprintId,
                    newStatus,
                    newPosition
            );
        }

        task.setStatus(newStatus);
        task.setPosition(newPosition);

        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(
                    Instant.now()
            );
        } else {
            task.setCompletedAt(null);
        }

        Task savedTask =
                taskRepository.save(task);

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "status",
                oldStatus
        );

        oldValue.put(
                "position",
                oldPosition
        );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "status",
                newStatus
        );

        newValue.put(
                "position",
                newPosition
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction
                                .TASK_STATUS_CHANGED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        sendStatusNotification(
                projectId,
                savedTask,
                currentUser.getId(),
                oldStatus,
                newStatus
        );

        return toResponse(savedTask);
    }

    // ===================== Đổi vị trí =====================
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            cacheNames = CacheNames.TASK_DETAIL,
                            key = "#projectId.toString() + ':' + #taskId.toString()"
                    ),
                    @CacheEvict(
                            cacheNames = CacheNames.TASK_SEARCH,
                            allEntries = true
                    ),
                    @CacheEvict(
                            cacheNames = CacheNames.SPRINT_KANBAN,
                            allEntries = true
                    ),
                    @CacheEvict(
                            cacheNames = CacheNames.SPRINT_TASK_STATISTICS,
                            allEntries = true
                    ),
                    @CacheEvict(
                            cacheNames = CacheNames.SPRINT_BURNDOWN,
                            allEntries = true
                    )
            }
    )
    public TaskResponse updatePosition(
            UUID projectId,
            UUID taskId,
            UpdateTaskPositionRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        TaskValidator.validateProjectEditable(
                project
        );

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        projectAccessService
                .requireTaskStatusUpdateAccess(
                        projectId,
                        currentUser,
                        task
                );

        if (task.getCurrentSprintId() == null) {
            throw new BusinessException(
                    ErrorCode.TASK_NOT_IN_ACTIVE_SPRINT
            );
        }

        Sprint sprint =
                sprintRepository
                        .findByIdAndProjectId(
                                task.getCurrentSprintId(),
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SPRINT_NOT_FOUND
                                )
                        );

        TaskValidator.validateSprintActive(
                sprint
        );

        return moveInsideColumn(
                projectId,
                task,
                request.position(),
                currentUser.getId()
        );
    }

    // ===================== HELPER =====================

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

    private void validateAssignee(
            UUID projectId,
            UUID assigneeUserId
    ) {
        User assignee =
                userRepository
                        .findById(assigneeUserId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        if (!assignee.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.TASK_ASSIGNEE_DISABLED
            );
        }

        boolean isMember =
                projectMemberRepository
                        .existsByProjectIdAndUserId(
                                projectId,
                                assigneeUserId
                        );

        if (!isMember) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_ASSIGNEE_NOT_PROJECT_MEMBER
            );
        }
    }

    private TaskResponse toResponse(
            Task task
    ) {
        User assignee = null;

        if (task.getAssigneeUserId() != null) {
            assignee =
                    userRepository
                            .findById(
                                    task.getAssigneeUserId()
                            )
                            .orElse(null);
        }

        Long spentMinutes =
                taskTimeLogRepository
                        .sumMinutesByTaskId(
                                task.getId()
                        );

        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getBacklogItemId(),
                task.getCurrentSprintId(),
                task.getTitle(),
                task.getDescription(),
                task.getType(),
                task.getStatus(),
                task.getPriority(),
                task.getAssigneeUserId(),
                assignee != null
                        ? assignee.getUsername()
                        : null,
                assignee != null
                        ? assignee.getEmail()
                        : null,
                task.getReporterUserId(),
                task.getEstimatedMinutes(),
                spentMinutes != null
                        ? spentMinutes
                        : 0L,
                task.getStartDate(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getPosition(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private KanbanTaskResponse toKanbanResponse(
            Task task
    ) {
        BacklogItem backlogItem =
                backlogItemRepository
                        .findByIdAndProjectId(
                                task.getBacklogItemId(),
                                task.getProjectId()
                        )
                        .orElse(null);

        User assignee = null;

        if (task.getAssigneeUserId() != null) {
            assignee =
                    userRepository
                            .findById(
                                    task.getAssigneeUserId()
                            )
                            .orElse(null);
        }

        Long spentMinutes =
                taskTimeLogRepository
                        .sumMinutesByTaskId(
                                task.getId()
                        );

        boolean overdue =
                task.getDueDate() != null
                        && task.getDueDate()
                        .isBefore(LocalDate.now())
                        && task.getStatus()
                        != TaskStatus.DONE
                        && task.getStatus()
                        != TaskStatus.CANCELLED;

        return new KanbanTaskResponse(
                task.getId(),
                task.getBacklogItemId(),
                backlogItem != null
                        ? backlogItem.getTitle()
                        : null,
                task.getTitle(),
                task.getType(),
                task.getStatus(),
                task.getPriority(),
                task.getAssigneeUserId(),
                assignee != null
                        ? assignee.getUsername()
                        : null,
                assignee != null
                        ? assignee.getEmail()
                        : null,
                task.getEstimatedMinutes(),
                spentMinutes != null
                        ? spentMinutes
                        : 0L,
                task.getStartDate(),
                task.getDueDate(),
                task.getPosition(),
                overdue,
                task.getStatus() == TaskStatus.BLOCKED
        );
    }

    private KanbanColumnResponse createColumn(
            TaskStatus status,
            String title,
            Map<TaskStatus, List<KanbanTaskResponse>>
                    tasksByStatus
    ) {
        List<KanbanTaskResponse> tasks =
                tasksByStatus.getOrDefault(
                        status,
                        List.of()
                );

        return new KanbanColumnResponse(
                status,
                title,
                tasks.size(),
                tasks
        );
    }

    private TaskResponse moveInsideColumn(
            UUID projectId,
            Task task,
            Long requestedPosition,
            UUID actorUserId
    ) {
        UUID sprintId =
                task.getCurrentSprintId();

        TaskStatus status =
                task.getStatus();

        Long oldPosition =
                task.getPosition();

        long columnCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintIdAndStatus(
                                projectId,
                                sprintId,
                                status
                        );

        long newPosition =
                Math.min(
                        requestedPosition,
                        Math.max(columnCount, 1L)
                );

        if (oldPosition.equals(newPosition)) {
            return toResponse(task);
        }

        if (newPosition < oldPosition) {
            taskRepository.moveRangeDown(
                    projectId,
                    sprintId,
                    status,
                    task.getId(),
                    newPosition,
                    oldPosition
            );
        } else {
            taskRepository.moveRangeUp(
                    projectId,
                    sprintId,
                    status,
                    task.getId(),
                    oldPosition,
                    newPosition
            );
        }

        task.setPosition(
                newPosition
        );

        Task savedTask =
                taskRepository.save(task);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TASK,
                        savedTask.getId(),
                        ProjectActivityAction
                                .TASK_POSITION_CHANGED,
                        actorUserId,
                        Map.of(
                                "position",
                                oldPosition
                        ),
                        Map.of(
                                "position",
                                newPosition
                        )
                )
        );

        return toResponse(savedTask);
    }

    private void sendStatusNotification(
            UUID projectId,
            Task task,
            UUID actorUserId,
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        UUID recipientUserId =
                task.getAssigneeUserId();

        if (recipientUserId == null) {
            return;
        }

        NotificationType type =
                newStatus == TaskStatus.BLOCKED
                        ? NotificationType.TASK_BLOCKED
                        : NotificationType.TASK_STATUS_CHANGED;

        String title =
                newStatus == TaskStatus.BLOCKED
                        ? "Task đang bị chặn"
                        : "Trạng thái Task đã thay đổi";

        String content =
                "Task "
                        + task.getTitle()
                        + " đã chuyển từ "
                        + oldStatus
                        + " sang "
                        + newStatus;

        notificationService.create(
                new NotificationCommand(
                        type,
                        title,
                        content,
                        actorUserId,
                        projectId,
                        ActivityEntityType.TASK,
                        task.getId(),
                        List.of(recipientUserId)
                )
        );
    }

    private Map<String, Object> taskSnapshot(
            Task task
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "title",
                task.getTitle()
        );

        value.put(
                "description",
                task.getDescription()
        );

        value.put(
                "type",
                task.getType()
        );

        value.put(
                "status",
                task.getStatus()
        );

        value.put(
                "priority",
                task.getPriority()
        );

        value.put(
                "assigneeUserId",
                task.getAssigneeUserId()
        );

        value.put(
                "estimatedMinutes",
                task.getEstimatedMinutes()
        );

        value.put(
                "startDate",
                task.getStartDate()
        );

        value.put(
                "dueDate",
                task.getDueDate()
        );

        value.put(
                "currentSprintId",
                task.getCurrentSprintId()
        );

        value.put(
                "position",
                task.getPosition()
        );

        return value;
    }

    private void sendAssignmentNotification(
            UUID projectId,
            Task task,
            UUID actorUserId,
            UUID assigneeUserId
    ) {
        notificationService.create(
                new NotificationCommand(
                        NotificationType.TASK_ASSIGNED,
                        "Bạn được phân công Task mới",
                        "Bạn được phân công Task "
                                + task.getTitle(),
                        actorUserId,
                        projectId,
                        ActivityEntityType.TASK,
                        task.getId(),
                        List.of(assigneeUserId)
                )
        );
    }

}