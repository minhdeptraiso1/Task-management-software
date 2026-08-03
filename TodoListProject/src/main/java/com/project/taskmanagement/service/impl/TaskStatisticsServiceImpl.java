package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.taskstatistics.*;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.service.TaskStatisticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.BacklogItemLookupHelper;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskStatisticsServiceImpl
        implements TaskStatisticsService {

    SprintRepository sprintRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    BacklogItemLookupHelper backlogItemLookupHelper;
    UserLookupHelper userLookupHelper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    // ===================== STATISTICS =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.SPRINT_TASK_STATISTICS,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #sprintId"
    )
    public SprintTaskStatisticsResponse getSprintStatistics(
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

        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintId(
                                projectId,
                                sprintId
                        );

        Map<UUID, Long> spentMinutesByTaskId =
                loadSpentMinutesByTaskId(tasks);

        long totalTasks =
                tasks.size();

        long completedTasks =
                countStatus(
                        tasks,
                        TaskStatus.DONE
                );

        long blockedTasks =
                countStatus(
                        tasks,
                        TaskStatus.BLOCKED
                );

        long cancelledTasks =
                countStatus(
                        tasks,
                        TaskStatus.CANCELLED
                );

        long unfinishedTasks =
                tasks.stream()
                        .filter(task ->
                                task.getStatus()
                                        != TaskStatus.DONE
                                        && task.getStatus()
                                        != TaskStatus.CANCELLED
                        )
                        .count();

        long overdueTasks =
                tasks.stream()
                        .filter(this::isOverdue)
                        .count();

        long estimatedMinutes =
                tasks.stream()
                        .map(Task::getEstimatedMinutes)
                        .filter(value -> value != null)
                        .mapToLong(Integer::longValue)
                        .sum();

        long spentMinutes =
                spentMinutesByTaskId
                        .values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .sum();

        long remainingEstimatedMinutes =
                Math.max(
                        estimatedMinutes
                                - spentMinutes,
                        0L
                );

        double completionRate =
                percentage(
                        completedTasks,
                        totalTasks
                );

        double timeUsageRate =
                percentage(
                        spentMinutes,
                        estimatedMinutes
                );

        boolean overEstimated =
                estimatedMinutes > 0
                        && spentMinutes
                        > estimatedMinutes;

        long overEstimatedMinutes =
                overEstimated
                        ? spentMinutes
                          - estimatedMinutes
                        : 0L;

        List<TaskStatusStatisticResponse> byStatus =
                buildStatusStatistics(
                        tasks
                );

        List<TaskAssigneeStatisticResponse> byAssignee =
                buildAssigneeStatistics(
                        tasks,
                        spentMinutesByTaskId
                );

        List<BacklogItemTaskStatisticResponse> byBacklogItem =
                buildBacklogStatistics(
                        projectId,
                        tasks,
                        spentMinutesByTaskId
                );

        return new SprintTaskStatisticsResponse(
                projectId,
                sprintId,
                sprint.getName(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                totalTasks,
                completedTasks,
                unfinishedTasks,
                blockedTasks,
                cancelledTasks,
                overdueTasks,
                completionRate,
                estimatedMinutes,
                spentMinutes,
                remainingEstimatedMinutes,
                timeUsageRate,
                overEstimated,
                overEstimatedMinutes,
                byStatus,
                byAssignee,
                byBacklogItem
        );
    }

    // ===================== BURNDOWN =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.SPRINT_BURNDOWN,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #sprintId"
    )
    public SprintBurndownResponse getSprintBurndown(
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

        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintId(
                                projectId,
                                sprintId
                        );

        LocalDate startDate =
                resolveSprintStartDate(sprint);

        LocalDate endDate =
                resolveSprintEndDate(
                        sprint,
                        startDate
                );

        long totalTasks =
                tasks.size();

        Map<LocalDate, Long> completedByDate =
                buildCompletedByDate(tasks);

        long totalDays =
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                ) + 1L;

        long cumulativeCompleted = 0L;

        List<BurndownPointResponse> points =
                new ArrayList<>();

        for (long dayIndex = 0;
             dayIndex < totalDays;
             dayIndex++) {

            LocalDate date =
                    startDate.plusDays(
                            dayIndex
                    );

            long completedOnDate =
                    completedByDate
                            .getOrDefault(
                                    date,
                                    0L
                            );

            cumulativeCompleted +=
                    completedOnDate;

            long actualRemaining =
                    Math.max(
                            totalTasks
                                    - cumulativeCompleted,
                            0L
                    );

            double idealRemaining;

            if (totalDays <= 1) {
                idealRemaining = 0.0;
            } else {
                idealRemaining =
                        totalTasks
                                * (
                                1.0
                                        - (
                                        (double) dayIndex
                                                / (totalDays - 1)
                                )
                        );
            }

            idealRemaining =
                    roundTwoDecimals(
                            Math.max(
                                    idealRemaining,
                                    0.0
                            )
                    );

            points.add(
                    new BurndownPointResponse(
                            date,
                            idealRemaining,
                            actualRemaining,
                            cumulativeCompleted,
                            completedOnDate
                    )
            );
        }

        return new SprintBurndownResponse(
                projectId,
                sprintId,
                sprint.getName(),
                sprint.getStatus(),
                startDate,
                endDate,
                totalTasks,
                points
        );
    }

    // ===================== STATUS =====================

    private List<TaskStatusStatisticResponse>
    buildStatusStatistics(
            List<Task> tasks
    ) {
        EnumMap<TaskStatus, Long> counts =
                new EnumMap<>(TaskStatus.class);

        for (TaskStatus status :
                TaskStatus.values()) {

            counts.put(status, 0L);
        }

        for (Task task : tasks) {
            counts.compute(
                    task.getStatus(),
                    (status, count) ->
                            count == null
                                    ? 1L
                                    : count + 1L
            );
        }

        long total =
                tasks.size();

        List<TaskStatusStatisticResponse> result =
                new ArrayList<>();

        for (TaskStatus status :
                TaskStatus.values()) {

            long count =
                    counts.getOrDefault(
                            status,
                            0L
                    );

            result.add(
                    new TaskStatusStatisticResponse(
                            status,
                            resolveStatusTitle(status),
                            count,
                            percentage(
                                    count,
                                    total
                            )
                    )
            );
        }

        return result;
    }

    // ===================== ASSIGNEE =====================

    private List<TaskAssigneeStatisticResponse>
    buildAssigneeStatistics(
            List<Task> tasks,
            Map<UUID, Long> spentMinutesByTaskId
    ) {
        Map<UUID, MutableAssigneeStatistic>
                statistics =
                new LinkedHashMap<>();

        /*
         * Dùng null để nhóm Task chưa được phân công.
         * HashMap/LinkedHashMap hỗ trợ key null.
         */
        for (Task task : tasks) {
            UUID assigneeUserId =
                    task.getAssigneeUserId();

            MutableAssigneeStatistic statistic =
                    statistics.computeIfAbsent(
                            assigneeUserId,
                            ignored ->
                                    new MutableAssigneeStatistic(
                                            assigneeUserId
                                    )
                    );

            statistic.totalTasks++;

            switch (task.getStatus()) {
                case TODO -> statistic.todoTasks++;

                case IN_PROGRESS -> statistic.inProgressTasks++;

                case IN_REVIEW -> statistic.inReviewTasks++;

                case DONE -> statistic.doneTasks++;

                case BLOCKED -> statistic.blockedTasks++;

                case CANCELLED -> statistic.cancelledTasks++;
            }

            if (task.getEstimatedMinutes()
                    != null) {

                statistic.estimatedMinutes +=
                        task.getEstimatedMinutes();
            }

            statistic.spentMinutes +=
                    spentMinutesByTaskId
                            .getOrDefault(
                                    task.getId(),
                                    0L
                            );
        }

        List<TaskAssigneeStatisticResponse> result =
                new ArrayList<>();

        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                statistics.keySet()
        );

        for (MutableAssigneeStatistic statistic :
                statistics.values()) {

            User user = userLookupHelper.getOrNull(usersById, statistic.userId);

            boolean overEstimated =
                    statistic.estimatedMinutes > 0
                            && statistic.spentMinutes
                            > statistic.estimatedMinutes;

            long overEstimatedMinutes =
                    overEstimated
                            ? statistic.spentMinutes
                              - statistic.estimatedMinutes
                            : 0L;

            result.add(
                    new TaskAssigneeStatisticResponse(
                            statistic.userId,
                            user != null
                                    ? user.getUsername()
                                    : "Chưa phân công",
                            user != null
                                    ? user.getEmail()
                                    : null,
                            statistic.totalTasks,
                            statistic.todoTasks,
                            statistic.inProgressTasks,
                            statistic.inReviewTasks,
                            statistic.doneTasks,
                            statistic.blockedTasks,
                            statistic.cancelledTasks,
                            statistic.estimatedMinutes,
                            statistic.spentMinutes,
                            percentage(
                                    statistic.doneTasks,
                                    statistic.totalTasks
                            ),
                            overEstimated,
                            overEstimatedMinutes
                    )
            );
        }

        result.sort(
                Comparator.comparingLong(
                        TaskAssigneeStatisticResponse
                                ::totalTasks
                ).reversed()
        );

        return result;
    }

    // ===================== BACKLOG ITEM =====================

    private List<BacklogItemTaskStatisticResponse>
    buildBacklogStatistics(
            UUID projectId,
            List<Task> tasks,
            Map<UUID, Long> spentMinutesByTaskId
    ) {
        Map<UUID, MutableBacklogStatistic>
                statistics =
                new LinkedHashMap<>();

        for (Task task : tasks) {
            MutableBacklogStatistic statistic =
                    statistics.computeIfAbsent(
                            task.getBacklogItemId(),
                            MutableBacklogStatistic::new
                    );

            statistic.totalTasks++;

            if (task.getStatus()
                    == TaskStatus.DONE) {

                statistic.completedTasks++;
            }

            if (task.getStatus()
                    != TaskStatus.DONE
                    && task.getStatus()
                    != TaskStatus.CANCELLED) {

                statistic.unfinishedTasks++;
            }

            if (task.getStatus()
                    == TaskStatus.BLOCKED) {

                statistic.blockedTasks++;
            }

            if (task.getEstimatedMinutes()
                    != null) {

                statistic.estimatedMinutes +=
                        task.getEstimatedMinutes();
            }

            statistic.spentMinutes +=
                    spentMinutesByTaskId
                            .getOrDefault(
                                    task.getId(),
                                    0L
                            );
        }

        List<BacklogItemTaskStatisticResponse> result =
                new ArrayList<>();

        Map<UUID, BacklogItem> backlogItemsById =
                backlogItemLookupHelper.findBacklogItemMap(statistics.keySet());

        for (MutableBacklogStatistic statistic :
                statistics.values()) {

            BacklogItem backlogItem = backlogItemLookupHelper.getOrNull(
                    backlogItemsById,
                    statistic.backlogItemId
            );

            result.add(
                    new BacklogItemTaskStatisticResponse(
                            statistic.backlogItemId,
                            backlogItem != null
                                    ? backlogItem.getTitle()
                                    : null,
                            statistic.totalTasks,
                            statistic.completedTasks,
                            statistic.unfinishedTasks,
                            statistic.blockedTasks,
                            statistic.estimatedMinutes,
                            statistic.spentMinutes,
                            percentage(
                                    statistic.completedTasks,
                                    statistic.totalTasks
                            )
                    )
            );
        }

        result.sort(
                Comparator.comparingLong(
                        BacklogItemTaskStatisticResponse
                                ::totalTasks
                ).reversed()
        );

        return result;
    }

    // ===================== TIME LOG =====================

    private Map<UUID, Long>
    loadSpentMinutesByTaskId(
            List<Task> tasks
    ) {
        if (tasks.isEmpty()) {
            return Map.of();
        }

        List<UUID> taskIds =
                tasks.stream()
                        .map(Task::getId)
                        .toList();

        List<Object[]> rows =
                taskTimeLogRepository
                        .sumMinutesGroupedByTaskIds(
                                taskIds
                        );

        Map<UUID, Long> result =
                new HashMap<>();

        for (Object[] row : rows) {
            UUID taskId =
                    (UUID) row[0];

            long spentMinutes =
                    row[1] instanceof Number number
                            ? number.longValue()
                            : 0L;

            result.put(
                    taskId,
                    spentMinutes
            );
        }

        return result;
    }

    // ===================== BURNDOWN HELPER =====================

    private Map<LocalDate, Long>
    buildCompletedByDate(
            List<Task> tasks
    ) {
        Map<LocalDate, Long> result =
                new HashMap<>();

        for (Task task : tasks) {
            if (task.getStatus()
                    != TaskStatus.DONE
                    || task.getCompletedAt()
                    == null) {

                continue;
            }

            LocalDate completedDate =
                    toLocalDate(
                            task.getCompletedAt()
                    );

            result.merge(
                    completedDate,
                    1L,
                    Long::sum
            );
        }

        return result;
    }

    private LocalDate resolveSprintStartDate(
            Sprint sprint
    ) {
        if (sprint.getStartDate() != null) {
            return sprint.getStartDate();
        }

        if (sprint.getStartedAt() != null) {
            return toLocalDate(
                    sprint.getStartedAt()
            );
        }

        if (sprint.getCreatedAt() != null) {
            return toLocalDate(
                    sprint.getCreatedAt()
            );
        }

        return LocalDate.now();
    }

    private LocalDate resolveSprintEndDate(
            Sprint sprint,
            LocalDate startDate
    ) {
        LocalDate endDate;

        if (sprint.getEndDate() != null) {
            endDate = sprint.getEndDate();

        } else if (sprint.getCompletedAt()
                != null) {

            endDate =
                    toLocalDate(
                            sprint.getCompletedAt()
                    );

        } else {
            endDate =
                    LocalDate.now();
        }

        if (endDate.isBefore(startDate)) {
            return startDate;
        }

        return endDate;
    }

    private LocalDate toLocalDate(
            Instant instant
    ) {
        return instant
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    // ===================== GENERAL HELPER =====================

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

    private long countStatus(
            List<Task> tasks,
            TaskStatus status
    ) {
        return tasks.stream()
                .filter(task ->
                        task.getStatus()
                                == status
                )
                .count();
    }

    private boolean isOverdue(
            Task task
    ) {
        return task.getDueDate() != null
                && task.getDueDate()
                .isBefore(LocalDate.now())
                && task.getStatus()
                != TaskStatus.DONE
                && task.getStatus()
                != TaskStatus.CANCELLED;
    }

    private double percentage(
            long value,
            long total
    ) {
        if (total <= 0) {
            return 0.0;
        }

        return roundTwoDecimals(
                value * 100.0 / total
        );
    }

    private double roundTwoDecimals(
            double value
    ) {
        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private String resolveStatusTitle(
            TaskStatus status
    ) {
        return switch (status) {
            case TODO -> "Cần làm";

            case IN_PROGRESS -> "Đang thực hiện";

            case IN_REVIEW -> "Đang kiểm tra";

            case DONE -> "Hoàn thành";

            case BLOCKED -> "Bị chặn";

            case CANCELLED -> "Đã hủy";
        };
    }

    // ===================== INTERNAL MODELS =====================

    private static class MutableAssigneeStatistic {

        final UUID userId;

        long totalTasks;
        long todoTasks;
        long inProgressTasks;
        long inReviewTasks;
        long doneTasks;
        long blockedTasks;
        long cancelledTasks;

        long estimatedMinutes;
        long spentMinutes;

        MutableAssigneeStatistic(
                UUID userId
        ) {
            this.userId = userId;
        }
    }

    private static class MutableBacklogStatistic {

        final UUID backlogItemId;

        long totalTasks;
        long completedTasks;
        long unfinishedTasks;
        long blockedTasks;

        long estimatedMinutes;
        long spentMinutes;

        MutableBacklogStatistic(
                UUID backlogItemId
        ) {
            this.backlogItemId =
                    backlogItemId;
        }
    }
}
