package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.tasktimelog.CreateTaskTimeLogRequest;
import com.project.taskmanagement.dto.request.tasktimelog.UpdateTaskTimeLogRequest;
import com.project.taskmanagement.dto.realtime.KanbanRealtimeEventType;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogPageResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeSummaryResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeUserSummaryResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.TaskTimeLogService;
import com.project.taskmanagement.service.TimeLogConcurrencyService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.cache.CacheEvictService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.realtime.KanbanRealtimePublisher;
import com.project.taskmanagement.service.validation.TaskTimeLogValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskTimeLogServiceImpl
        implements TaskTimeLogService {

    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    UserLookupHelper userLookupHelper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    ProjectActivityService projectActivityService;
    KanbanRealtimePublisher kanbanRealtimePublisher;
    CacheEvictService cacheEvictService;
    TimeLogConcurrencyService timeLogConcurrencyService;

    // ===================== CREATE =====================

    @Override
    @Transactional
    public TaskTimeLogResponse create(
            UUID projectId,
            UUID taskId,
            CreateTaskTimeLogRequest request
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

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        projectAccessService
                .requireTaskTimeLogCreateAccess(
                        projectId,
                        currentUser,
                        task
                );

        TaskTimeLogValidator.validateTask(
                task
        );

        TaskTimeLogValidator.validateWorkDate(
                request.workDate()
        );

        TaskTimeLogValidator.validateMinutes(
                request.minutes()
        );

        timeLogConcurrencyService.lockUserWorkDate(
                currentUser.getId(),
                request.workDate()
        );

        Long currentDailyMinutes =
                taskTimeLogRepository
                        .sumMinutesByUserIdAndWorkDate(
                                currentUser.getId(),
                                request.workDate()
                        );

        TaskTimeLogValidator.validateDailyLimit(
                currentDailyMinutes != null
                        ? currentDailyMinutes
                        : 0L,
                request.minutes()
        );

        TaskTimeLog timeLog =
                TaskTimeLog.builder()
                        .taskId(taskId)
                        .userId(
                                currentUser.getId()
                        )
                        .workDate(
                                request.workDate()
                        )
                        .minutes(
                                request.minutes()
                        )
                        .description(
                                TaskTimeLogValidator
                                        .normalizeDescription(
                                                request.description()
                                        )
                        )
                        .build();

        TaskTimeLog savedTimeLog =
                taskTimeLogRepository.save(
                        timeLog
                );

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "taskId",
                taskId
        );

        newValue.put(
                "userId",
                currentUser.getId()
        );

        newValue.put(
                "workDate",
                savedTimeLog.getWorkDate()
        );

        newValue.put(
                "minutes",
                savedTimeLog.getMinutes()
        );

        newValue.put(
                "description",
                savedTimeLog.getDescription()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TIME_LOG,
                        savedTimeLog.getId(),
                        ProjectActivityAction
                                .TIME_LOG_CREATED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        kanbanRealtimePublisher.publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_TIME_LOGGED,
                currentUser.getId(),
                currentUser.getUsername()
        );

        cacheEvictService.evictTaskTimeLogs(
                projectId,
                task.getCurrentSprintId(),
                taskId,
                currentUser.getId()
        );

        return toResponse(
                projectId,
                savedTimeLog,
                currentUser
        );
    }

    // ===================== GET =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.TASK_TIME_LOG_LIST,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #taskId" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public TaskTimeLogPageResponse getTimeLogs(
            UUID projectId,
            UUID taskId,
            Pageable pageable
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

        Task task =
                getTaskOrThrow(
                projectId,
                taskId
        );

        Page<TaskTimeLog> page =
                taskTimeLogRepository
                        .findAllByTaskIdOrderByWorkDateDescCreatedAtDesc(
                                taskId,
                                pageable
                        );

        boolean canModerate =
                projectAccessService
                        .canModerateTaskTimeLogs(
                                projectId,
                                currentUser
                        );

        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                page.getContent().stream().map(TaskTimeLog::getUserId).toList()
        );
        List<TaskTimeLogResponse> responses =
                page.getContent()
                        .stream()
                        .map(timeLog ->
                                toResponse(
                                        timeLog,
                                        currentUser,
                                        canModerate,
                                        usersById
                                )
                        )
                        .toList();

        return new TaskTimeLogPageResponse(
                responses,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }

    // ===================== UPDATE =====================

    @Override
    @Transactional
    public TaskTimeLogResponse update(
            UUID projectId,
            UUID taskId,
            UUID timeLogId,
            UpdateTaskTimeLogRequest request
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

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        TaskTimeLog timeLog =
                getTimeLogOrThrow(
                        taskId,
                        timeLogId
                );

        if (!timeLog.getUserId()
                .equals(currentUser.getId())) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_ACCESS_DENIED
            );
        }

        LocalDate newWorkDate =
                request.workDate() != null
                        ? request.workDate()
                        : timeLog.getWorkDate();

        Integer newMinutes =
                request.minutes() != null
                        ? request.minutes()
                        : timeLog.getMinutes();

        TaskTimeLogValidator.validateWorkDate(
                newWorkDate
        );

        TaskTimeLogValidator.validateMinutes(
                newMinutes
        );

        lockForTimeLogUpdate(
                timeLog.getUserId(),
                timeLog.getWorkDate(),
                newWorkDate
        );

        Long currentDailyMinutes =
                taskTimeLogRepository
                        .sumMinutesByUserIdAndWorkDateExcludingId(
                                currentUser.getId(),
                                newWorkDate,
                                timeLogId
                        );

        TaskTimeLogValidator.validateDailyLimit(
                currentDailyMinutes != null
                        ? currentDailyMinutes
                        : 0L,
                newMinutes
        );

        Map<String, Object> oldValue =
                timeLogSnapshot(timeLog);

        if (request.workDate() != null) {
            timeLog.setWorkDate(
                    request.workDate()
            );
        }

        if (request.minutes() != null) {
            timeLog.setMinutes(
                    request.minutes()
            );
        }

        if (request.description() != null) {
            timeLog.setDescription(
                    TaskTimeLogValidator
                            .normalizeDescription(
                                    request.description()
                            )
            );
        }

        TaskTimeLog savedTimeLog =
                taskTimeLogRepository.save(
                        timeLog
                );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TIME_LOG,
                        savedTimeLog.getId(),
                        ProjectActivityAction
                                .TIME_LOG_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        timeLogSnapshot(
                                savedTimeLog
                        )
                )
        );

        kanbanRealtimePublisher.publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_TIME_LOGGED,
                currentUser.getId(),
                currentUser.getUsername()
        );

        cacheEvictService.evictTaskTimeLogs(
                projectId,
                task.getCurrentSprintId(),
                taskId,
                currentUser.getId()
        );

        return toResponse(
                projectId,
                savedTimeLog,
                currentUser
        );
    }

    // ===================== DELETE =====================

    @Override
    @Transactional
    public void delete(
            UUID projectId,
            UUID taskId,
            UUID timeLogId
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

        Task task =
                getTaskOrThrow(
                projectId,
                taskId
        );

        TaskTimeLog timeLog =
                getTimeLogOrThrow(
                        taskId,
                        timeLogId
                );

        boolean isOwner =
                timeLog.getUserId()
                        .equals(
                                currentUser.getId()
                        );

        boolean canModerate =
                projectAccessService
                        .canModerateTaskTimeLogs(
                                projectId,
                                currentUser
                        );

        if (!isOwner) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_TIME_LOG_ACCESS_DENIED
            );
        }

        timeLogConcurrencyService.lockUserWorkDate(
                timeLog.getUserId(),
                timeLog.getWorkDate()
        );

        Map<String, Object> oldValue =
                timeLogSnapshot(timeLog);

        timeLog.markDeleted(
                currentUser.getUsername()
        );

        taskTimeLogRepository.save(
                timeLog
        );

        cacheEvictService.evictTaskTimeLogs(
                projectId,
                task.getCurrentSprintId(),
                taskId,
                currentUser.getId()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.TIME_LOG,
                        timeLogId,
                        ProjectActivityAction
                                .TIME_LOG_DELETED,
                        currentUser.getId(),
                        oldValue,
                        null
                )
        );

        kanbanRealtimePublisher.publishGeneric(
                task,
                KanbanRealtimeEventType.TASK_TIME_LOGGED,
                currentUser.getId(),
                currentUser.getUsername()
        );
    }

    // ===================== SUMMARY =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.TASK_TIME_SUMMARY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #taskId"
    )
    public TaskTimeSummaryResponse getSummary(
            UUID projectId,
            UUID taskId
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

        Task task =
                getTaskOrThrow(
                        projectId,
                        taskId
                );

        Long spentMinutes =
                taskTimeLogRepository
                        .sumMinutesByTaskId(
                                taskId
                        );

        if (spentMinutes == null) {
            spentMinutes = 0L;
        }

        Integer estimatedMinutes =
                task.getEstimatedMinutes();

        Long remainingMinutes = null;
        Double progressPercentage = null;
        boolean overEstimated = false;
        Long overEstimatedMinutes = 0L;

        if (estimatedMinutes != null) {
            remainingMinutes =
                    Math.max(
                            estimatedMinutes
                                    - spentMinutes,
                            0L
                    );

            if (estimatedMinutes > 0) {
                progressPercentage =
                        Math.round(
                                (
                                        spentMinutes
                                                * 10000.0
                                )
                                        / estimatedMinutes
                        ) / 100.0;
            } else {
                progressPercentage =
                        spentMinutes > 0
                                ? 100.0
                                : 0.0;
            }

            overEstimated =
                    spentMinutes
                            > estimatedMinutes;

            if (overEstimated) {
                overEstimatedMinutes =
                        spentMinutes
                                - estimatedMinutes;
            }
        }

        List<TaskTimeUserSummaryResponse> byUser =
                loadUserSummary(taskId);

        return new TaskTimeSummaryResponse(
                taskId,
                estimatedMinutes,
                spentMinutes,
                remainingMinutes,
                progressPercentage,
                overEstimated,
                overEstimatedMinutes,
                byUser
        );
    }

    // ===================== RESPONSE =====================

    private TaskTimeLogResponse toResponse(
            UUID projectId,
            TaskTimeLog timeLog,
            User currentUser
    ) {
        boolean canModerate =
                projectAccessService
                        .canModerateTaskTimeLogs(
                                projectId,
                                currentUser
                        );

        return toResponse(
                timeLog,
                currentUser,
                canModerate,
                userLookupHelper.findUserMap(Collections.singletonList(timeLog.getUserId()))
        );
    }

    private TaskTimeLogResponse toResponse(
            TaskTimeLog timeLog,
            User currentUser,
            boolean canModerate,
            Map<UUID, User> usersById
    ) {
        User author = userLookupHelper.getOrNull(usersById, timeLog.getUserId());

        boolean canEdit =
                timeLog.getUserId()
                        .equals(
                                currentUser.getId()
                        );

        boolean canDelete =
                canEdit;

        return new TaskTimeLogResponse(
                timeLog.getId(),
                timeLog.getTaskId(),
                timeLog.getUserId(),
                author != null
                        ? author.getUsername()
                        : null,
                author != null
                        ? author.getEmail()
                        : null,
                timeLog.getWorkDate(),
                timeLog.getMinutes(),
                timeLog.getDescription(),
                canEdit,
                canDelete,
                timeLog.getCreatedAt(),
                timeLog.getUpdatedAt()
        );
    }

    // ===================== USER SUMMARY =====================

    private List<TaskTimeUserSummaryResponse>
    loadUserSummary(
            UUID taskId
    ) {
        List<Object[]> rows =
                taskTimeLogRepository
                        .sumMinutesGroupedByUser(
                                taskId
                        );

        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                rows.stream()
                        .filter(Objects::nonNull)
                        .filter(row -> row.length > 0)
                        .map(row -> (UUID) row[0])
                        .toList()
        );

        List<TaskTimeUserSummaryResponse> result =
                new ArrayList<>();

        for (Object[] row : rows) {
            UUID userId =
                    (UUID) row[0];

            Long minutes =
                    row[1] instanceof Long value
                            ? value
                            : ((Number) row[1])
                              .longValue();

            User user = userLookupHelper.getOrNull(usersById, userId);

            result.add(
                    new TaskTimeUserSummaryResponse(
                            userId,
                            user != null
                                    ? user.getUsername()
                                    : null,
                            user != null
                                    ? user.getEmail()
                                    : null,
                            minutes
                    )
            );
        }

        return result;
    }

    // ===================== SNAPSHOT =====================

    private Map<String, Object> timeLogSnapshot(
            TaskTimeLog timeLog
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "taskId",
                timeLog.getTaskId()
        );

        value.put(
                "userId",
                timeLog.getUserId()
        );

        value.put(
                "workDate",
                timeLog.getWorkDate()
        );

        value.put(
                "minutes",
                timeLog.getMinutes()
        );

        value.put(
                "description",
                timeLog.getDescription()
        );

        return value;
    }

    // ===================== HELPER =====================

    private void lockForTimeLogUpdate(
            UUID userId,
            LocalDate oldWorkDate,
            LocalDate newWorkDate
    ) {
        java.util.stream.Stream
                .of(oldWorkDate, newWorkDate)
                .distinct()
                .sorted()
                .forEach(workDate ->
                        timeLogConcurrencyService.lockUserWorkDate(
                                userId,
                                workDate
                        )
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

    private TaskTimeLog getTimeLogOrThrow(
            UUID taskId,
            UUID timeLogId
    ) {
        return taskTimeLogRepository
                .findByIdAndTaskId(
                        timeLogId,
                        taskId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .TASK_TIME_LOG_NOT_FOUND
                        )
                );
    }
}
