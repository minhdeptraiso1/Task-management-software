package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.report.ProjectMemberReportRequest;
import com.project.taskmanagement.dto.request.report.ProjectTimeReportRequest;
import com.project.taskmanagement.dto.response.report.*;
import com.project.taskmanagement.dto.response.taskstatistics.SprintBurndownResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.repository.projection.report.ProjectTimeDailyView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeMemberView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeTaskView;
import com.project.taskmanagement.service.ProjectReportService;
import com.project.taskmanagement.service.TaskStatisticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectReportServiceImpl
        implements ProjectReportService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    UserRepository userRepository;

    TaskStatisticsService taskStatisticsService;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final long MAX_REPORT_DAYS = 366L;

    static final List<TaskStatus> TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    // ===================== SPRINT REPORT =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_REPORT_SPRINT,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #sprintId"
    )
    public SprintReportResponse getSprintReport(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(
                projectId
        );

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

        List<BacklogItem> sprintItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        long totalStoryPoints =
                sprintItems.stream()
                        .map(BacklogItem::getStoryPoints)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum();

        SprintTaskStatisticsResponse statistics =
                taskStatisticsService
                        .getSprintStatistics(
                                projectId,
                                sprintId
                        );

        SprintBurndownResponse burndown =
                taskStatisticsService
                        .getSprintBurndown(
                                projectId,
                                sprintId
                        );

        return new SprintReportResponse(
                projectId,
                sprint.getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStartedAt(),
                sprint.getCompletedAt(),
                sprintItems.size(),
                totalStoryPoints,
                statistics,
                burndown,
                Instant.now()
        );
    }

    // ===================== MEMBER REPORT =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_REPORT_MEMBER,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + '|from=' + (#request == null || #request.fromDate() == null ? '' : #request.fromDate())" +
                    " + '|to=' + (#request == null || #request.toDate() == null ? '' : #request.toDate())" +
                    " + '|user=' + (#request == null || #request.userId() == null ? '' : #request.userId())"
    )
    public ProjectMemberReportResponse getMemberReport(
            UUID projectId,
            ProjectMemberReportRequest request
    ) {
        Project project =
                requireViewAccess(
                        projectId
                );

        DateRange dateRange =
                resolveDateRange(
                        project,
                        request != null
                                ? request.fromDate()
                                : null,
                        request != null
                                ? request.toDate()
                                : null
                );

        UUID filteredUserId =
                request != null
                        ? request.userId()
                        : null;

        validateReportUser(
                projectId,
                filteredUserId
        );

        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        if (filteredUserId != null) {
            members =
                    members.stream()
                            .filter(member ->
                                    filteredUserId.equals(
                                            member.getUserId()
                                    )
                            )
                            .toList();
        }

        List<Task> projectTasks =
                taskRepository
                        .findAllByProjectId(
                                projectId
                        );

        Map<UUID, List<Task>> tasksByAssignee =
                projectTasks.stream()
                        .filter(task ->
                                task.getAssigneeUserId() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        Task::getAssigneeUserId,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        List<ProjectTimeMemberView> timeRows =
                taskTimeLogRepository
                        .summarizeProjectTimeByMember(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                filteredUserId,
                                null
                        );

        Map<UUID, ProjectTimeMemberView> timeByUserId =
                timeRows.stream()
                        .collect(
                                Collectors.toMap(
                                        ProjectTimeMemberView::getUserId,
                                        Function.identity(),
                                        (left, right) -> left,
                                        LinkedHashMap::new
                                )
                        );

        Map<UUID, User> usersById =
                loadUsersByMembers(
                        members
                );

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        List<ProjectMemberReportItemResponse> items =
                new ArrayList<>();

        long totalAssignedTasks = 0L;
        long totalEstimatedMinutes = 0L;
        long totalSpentMinutes = 0L;
        long membersWithTasks = 0L;
        long membersWithTimeLogs = 0L;

        for (ProjectMember member : members) {
            User user =
                    usersById.get(
                            member.getUserId()
                    );

            List<Task> userTasks =
                    tasksByAssignee.getOrDefault(
                            member.getUserId(),
                            List.of()
                    );

            ProjectTimeMemberView timeView =
                    timeByUserId.get(
                            member.getUserId()
                    );

            MemberStatistics statistics =
                    calculateMemberStatistics(
                            userTasks,
                            timeView,
                            today
                    );

            if (statistics.totalTasks() > 0) {
                membersWithTasks++;
            }

            if (statistics.spentMinutes() > 0) {
                membersWithTimeLogs++;
            }

            totalAssignedTasks +=
                    statistics.totalTasks();

            totalEstimatedMinutes +=
                    statistics.estimatedMinutes();

            totalSpentMinutes +=
                    statistics.spentMinutes();

            items.add(
                    new ProjectMemberReportItemResponse(
                            member.getUserId(),
                            user != null
                                    ? user.getUsername()
                                    : "Người dùng không tồn tại",
                            user != null
                                    ? user.getEmail()
                                    : null,
                            user != null
                                    ? user.getRole()
                                    : null,
                            member.getRole(),
                            statistics.totalTasks(),
                            statistics.activeTasks(),
                            statistics.todoTasks(),
                            statistics.inProgressTasks(),
                            statistics.inReviewTasks(),
                            statistics.doneTasks(),
                            statistics.blockedTasks(),
                            statistics.cancelledTasks(),
                            statistics.overdueTasks(),
                            statistics.estimatedMinutes(),
                            statistics.spentMinutes(),
                            percentage(
                                    statistics.doneTasks(),
                                    statistics.totalTasks()
                            ),
                            percentage(
                                    statistics.spentMinutes(),
                                    statistics.estimatedMinutes()
                            ),
                            statistics.overEstimated(),
                            statistics.overEstimatedMinutes(),
                            statistics.firstWorkDate(),
                            statistics.lastWorkDate()
                    )
            );
        }

        items.sort(
                Comparator.comparingLong(
                                ProjectMemberReportItemResponse
                                        ::activeTasks
                        )
                        .reversed()
                        .thenComparing(
                                item ->
                                        item.username() == null
                                                ? ""
                                                : item.username()
                        )
        );

        return new ProjectMemberReportResponse(
                projectId,
                project.getCode(),
                project.getName(),
                dateRange.fromDate(),
                dateRange.toDate(),
                members.size(),
                membersWithTasks,
                membersWithTimeLogs,
                totalAssignedTasks,
                totalEstimatedMinutes,
                totalSpentMinutes,
                items,
                Instant.now()
        );
    }

    // ===================== TIME REPORT =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_REPORT_TIME,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + '|from=' + (#request == null || #request.fromDate() == null ? '' : #request.fromDate())" +
                    " + '|to=' + (#request == null || #request.toDate() == null ? '' : #request.toDate())" +
                    " + '|user=' + (#request == null || #request.userId() == null ? '' : #request.userId())" +
                    " + '|task=' + (#request == null || #request.taskId() == null ? '' : #request.taskId())"
    )
    public ProjectTimeReportResponse getTimeReport(
            UUID projectId,
            ProjectTimeReportRequest request
    ) {
        Project project =
                requireViewAccess(
                        projectId
                );

        DateRange dateRange =
                resolveDateRange(
                        project,
                        request != null
                                ? request.fromDate()
                                : null,
                        request != null
                                ? request.toDate()
                                : null
                );

        UUID userId =
                request != null
                        ? request.userId()
                        : null;

        UUID taskId =
                request != null
                        ? request.taskId()
                        : null;

        validateReportUser(
                projectId,
                userId
        );

        validateReportTask(
                projectId,
                taskId
        );

        long totalMinutes =
                safeLong(
                        taskTimeLogRepository
                                .sumProjectMinutes(
                                        projectId,
                                        dateRange.fromDate(),
                                        dateRange.toDate(),
                                        userId,
                                        taskId
                                )
                );

        long totalLogs =
                taskTimeLogRepository
                        .countProjectTimeLogs(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        );

        long taskCount =
                taskTimeLogRepository
                        .countDistinctTasksWithTimeLog(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        );

        long contributorCount =
                taskTimeLogRepository
                        .countDistinctContributors(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        );

        long totalEstimatedMinutes =
                safeLong(
                        taskRepository
                                .sumEstimatedMinutesForReport(
                                        projectId,
                                        userId,
                                        taskId
                                )
                );

        long remainingEstimatedMinutes =
                Math.max(
                        totalEstimatedMinutes
                                - totalMinutes,
                        0L
                );

        boolean overEstimated =
                totalEstimatedMinutes > 0
                        && totalMinutes
                        > totalEstimatedMinutes;

        long overEstimatedMinutes =
                overEstimated
                        ? totalMinutes
                          - totalEstimatedMinutes
                        : 0L;

        List<ProjectTimeDailyResponse> byDate =
                buildDailyReport(
                        projectId,
                        dateRange,
                        userId,
                        taskId
                );

        List<ProjectTimeMemberResponse> byMember =
                buildMemberTimeReport(
                        projectId,
                        dateRange,
                        userId,
                        taskId,
                        totalMinutes
                );

        List<ProjectTimeTaskResponse> byTask =
                buildTaskTimeReport(
                        projectId,
                        dateRange,
                        userId,
                        taskId
                );

        return new ProjectTimeReportResponse(
                projectId,
                project.getCode(),
                project.getName(),
                dateRange.fromDate(),
                dateRange.toDate(),
                userId,
                taskId,
                totalMinutes,
                totalLogs,
                taskCount,
                contributorCount,
                totalEstimatedMinutes,
                remainingEstimatedMinutes,
                percentage(
                        totalMinutes,
                        totalEstimatedMinutes
                ),
                overEstimated,
                overEstimatedMinutes,
                byDate,
                byMember,
                byTask,
                Instant.now()
        );
    }

    // ===================== DAILY REPORT =====================

    private List<ProjectTimeDailyResponse>
    buildDailyReport(
            UUID projectId,
            DateRange dateRange,
            UUID userId,
            UUID taskId
    ) {
        Map<LocalDate, ProjectTimeDailyView> rowsByDate =
                taskTimeLogRepository
                        .summarizeProjectTimeByDate(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ProjectTimeDailyView::getWorkDate,
                                        Function.identity(),
                                        (left, right) -> left,
                                        LinkedHashMap::new
                                )
                        );

        List<ProjectTimeDailyResponse> result =
                new ArrayList<>();

        LocalDate currentDate =
                dateRange.fromDate();

        while (!currentDate.isAfter(
                dateRange.toDate()
        )) {
            ProjectTimeDailyView row =
                    rowsByDate.get(currentDate);

            result.add(
                    new ProjectTimeDailyResponse(
                            currentDate,
                            row != null
                                    ? safeLong(
                                    row.getSpentMinutes()
                            )
                                    : 0L,
                            row != null
                                    ? safeLong(
                                    row.getLogCount()
                            )
                                    : 0L
                    )
            );

            currentDate =
                    currentDate.plusDays(1);
        }

        return result;
    }

    // ===================== MEMBER TIME REPORT =====================

    private List<ProjectTimeMemberResponse>
    buildMemberTimeReport(
            UUID projectId,
            DateRange dateRange,
            UUID userId,
            UUID taskId,
            long totalMinutes
    ) {
        List<ProjectTimeMemberView> rows =
                taskTimeLogRepository
                        .summarizeProjectTimeByMember(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        );

        List<UUID> userIds =
                rows.stream()
                        .map(
                                ProjectTimeMemberView::getUserId
                        )
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<UUID, User> usersById =
                userRepository
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

        return rows.stream()
                .map(row -> {
                    User user =
                            usersById.get(
                                    row.getUserId()
                            );

                    long spentMinutes =
                            safeLong(
                                    row.getSpentMinutes()
                            );

                    return new ProjectTimeMemberResponse(
                            row.getUserId(),
                            user != null
                                    ? user.getUsername()
                                    : "Người dùng không tồn tại",
                            user != null
                                    ? user.getEmail()
                                    : null,
                            spentMinutes,
                            safeLong(row.getLogCount()),
                            safeLong(row.getTaskCount()),
                            percentage(
                                    spentMinutes,
                                    totalMinutes
                            )
                    );
                })
                .toList();
    }

    // ===================== TASK TIME REPORT =====================

    private List<ProjectTimeTaskResponse>
    buildTaskTimeReport(
            UUID projectId,
            DateRange dateRange,
            UUID userId,
            UUID taskId
    ) {
        List<ProjectTimeTaskView> rows =
                taskTimeLogRepository
                        .summarizeProjectTimeByTask(
                                projectId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                userId,
                                taskId
                        );

        List<UUID> taskIds =
                rows.stream()
                        .map(
                                ProjectTimeTaskView::getTaskId
                        )
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<UUID, Task> tasksById =
                taskRepository
                        .findAllById(taskIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Task::getId,
                                        Function.identity(),
                                        (left, right) -> left,
                                        LinkedHashMap::new
                                )
                        );

        List<ProjectTimeTaskResponse> result =
                new ArrayList<>();

        for (ProjectTimeTaskView row : rows) {
            Task task =
                    tasksById.get(
                            row.getTaskId()
                    );

            if (task == null) {
                continue;
            }

            long spentMinutes =
                    safeLong(
                            row.getSpentMinutes()
                    );

            long estimatedMinutes =
                    task.getEstimatedMinutes() == null
                            ? 0L
                            : task.getEstimatedMinutes();

            boolean overEstimated =
                    estimatedMinutes > 0
                            && spentMinutes
                            > estimatedMinutes;

            result.add(
                    new ProjectTimeTaskResponse(
                            task.getId(),
                            task.getTitle(),
                            task.getStatus(),
                            task.getAssigneeUserId(),
                            task.getEstimatedMinutes(),
                            spentMinutes,
                            safeLong(row.getLogCount()),
                            safeLong(
                                    row.getContributorCount()
                            ),
                            percentage(
                                    spentMinutes,
                                    estimatedMinutes
                            ),
                            overEstimated,
                            overEstimated
                                    ? spentMinutes
                                      - estimatedMinutes
                                    : 0L,
                            "/projects/"
                                    + projectId
                                    + "/tasks/"
                                    + task.getId()
                    )
            );
        }

        return result;
    }

    // ===================== MEMBER CALCULATION =====================

    private MemberStatistics calculateMemberStatistics(
            List<Task> tasks,
            ProjectTimeMemberView timeView,
            LocalDate today
    ) {
        long totalTasks =
                tasks.size();

        long todoTasks =
                countStatus(
                        tasks,
                        TaskStatus.TODO
                );

        long inProgressTasks =
                countStatus(
                        tasks,
                        TaskStatus.IN_PROGRESS
                );

        long inReviewTasks =
                countStatus(
                        tasks,
                        TaskStatus.IN_REVIEW
                );

        long doneTasks =
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

        long activeTasks =
                totalTasks
                        - doneTasks
                        - cancelledTasks;

        long overdueTasks =
                tasks.stream()
                        .filter(task ->
                                task.getDueDate() != null
                                        && task.getDueDate()
                                        .isBefore(today)
                                        && !TERMINAL_STATUSES
                                        .contains(
                                                task.getStatus()
                                        )
                        )
                        .count();

        long estimatedMinutes =
                tasks.stream()
                        .map(Task::getEstimatedMinutes)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum();

        long spentMinutes =
                timeView != null
                        ? safeLong(
                        timeView.getSpentMinutes()
                )
                        : 0L;

        boolean overEstimated =
                estimatedMinutes > 0
                        && spentMinutes
                        > estimatedMinutes;

        return new MemberStatistics(
                totalTasks,
                activeTasks,
                todoTasks,
                inProgressTasks,
                inReviewTasks,
                doneTasks,
                blockedTasks,
                cancelledTasks,
                overdueTasks,
                estimatedMinutes,
                spentMinutes,
                overEstimated,
                overEstimated
                        ? spentMinutes
                          - estimatedMinutes
                        : 0L,
                timeView != null
                        ? timeView.getFirstWorkDate()
                        : null,
                timeView != null
                        ? timeView.getLastWorkDate()
                        : null
        );
    }

    // ===================== VALIDATION =====================

    private Project requireViewAccess(
            UUID projectId
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

        return project;
    }

    private void validateReportUser(
            UUID projectId,
            UUID userId
    ) {
        if (userId == null) {
            return;
        }

        boolean memberExists =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                projectId,
                                userId
                        )
                        .isPresent();

        if (!memberExists) {
            throw new BusinessException(
                    ErrorCode
                            .REPORT_USER_NOT_PROJECT_MEMBER
            );
        }
    }

    private void validateReportTask(
            UUID projectId,
            UUID taskId
    ) {
        if (taskId == null) {
            return;
        }

        boolean exists =
                taskRepository
                        .findByIdAndProjectId(
                                taskId,
                                projectId
                        )
                        .isPresent();

        if (!exists) {
            throw new BusinessException(
                    ErrorCode
                            .REPORT_TASK_NOT_IN_PROJECT
            );
        }
    }

    private DateRange resolveDateRange(
            Project project,
            LocalDate requestedFromDate,
            LocalDate requestedToDate
    ) {
        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        LocalDate fromDate =
                requestedFromDate != null
                        ? requestedFromDate
                        : project.getStartDate();

        if (fromDate == null) {
            fromDate =
                    today.withDayOfMonth(1);
        }

        LocalDate toDate =
                requestedToDate != null
                        ? requestedToDate
                        : today;

        if (fromDate.isAfter(toDate)) {
            throw new BusinessException(
                    ErrorCode
                            .REPORT_DATE_RANGE_INVALID
            );
        }

        long days =
                ChronoUnit.DAYS.between(
                        fromDate,
                        toDate
                ) + 1L;

        if (days > MAX_REPORT_DAYS) {
            throw new BusinessException(
                    ErrorCode
                            .REPORT_DATE_RANGE_TOO_LARGE
            );
        }

        return new DateRange(
                fromDate,
                toDate
        );
    }

    // ===================== USER LOAD =====================

    private Map<UUID, User> loadUsersByMembers(
            Collection<ProjectMember> members
    ) {
        if (members == null
                || members.isEmpty()) {

            return new LinkedHashMap<>();
        }

        List<UUID> userIds =
                members.stream()
                        .map(ProjectMember::getUserId)
                        .distinct()
                        .toList();

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

    // ===================== HELPERS =====================

    private long countStatus(
            List<Task> tasks,
            TaskStatus status
    ) {
        return tasks.stream()
                .filter(task ->
                        task.getStatus() == status
                )
                .count();
    }

    private long safeLong(
            Number value
    ) {
        return value == null
                ? 0L
                : value.longValue();
    }

    private double percentage(
            long value,
            long total
    ) {
        if (total <= 0L) {
            return 0.0;
        }

        return Math.round(
                value * 10000.0 / total
        ) / 100.0;
    }

    // ===================== INTERNAL MODELS =====================

    private record DateRange(

            LocalDate fromDate,

            LocalDate toDate

    ) {
    }

    private record MemberStatistics(

            long totalTasks,

            long activeTasks,

            long todoTasks,

            long inProgressTasks,

            long inReviewTasks,

            long doneTasks,

            long blockedTasks,

            long cancelledTasks,

            long overdueTasks,

            long estimatedMinutes,

            long spentMinutes,

            boolean overEstimated,

            long overEstimatedMinutes,

            LocalDate firstWorkDate,

            LocalDate lastWorkDate

    ) {
    }
}
