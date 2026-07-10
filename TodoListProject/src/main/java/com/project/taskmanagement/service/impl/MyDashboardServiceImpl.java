package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.dashboard.MyTaskSearchRequest;
import com.project.taskmanagement.dto.response.dashboard.*;
import com.project.taskmanagement.dto.response.taskrisk.MyTaskRiskSummaryResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.TaskRiskLevel;
import com.project.taskmanagement.enums.TaskRiskReason;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.repository.spec.TaskSpecification;
import com.project.taskmanagement.service.MyDashboardService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class MyDashboardServiceImpl
        implements MyDashboardService {

    TaskRepository taskRepository;
    TaskDependencyRepository taskDependencyRepository;
    TaskTimeLogRepository taskTimeLogRepository;

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;

    NotificationRecipientRepository
            notificationRecipientRepository;

    CurrentUserService currentUserService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DUE_SOON_DAYS = 3;

    static final int DASHBOARD_TASK_LIMIT = 5;

    static final List<TaskStatus> TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    // ===================== DASHBOARD =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.MY_DASHBOARD,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Ho_Chi_Minh'))"
    )
    public MyDashboardResponse getMyDashboard() {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        long unreadNotifications =
                notificationRecipientRepository
                        .countUnreadByUserId(
                                currentUser.getId()
                        );

        /*
         * ADMIN không phải thành viên nghiệp vụ Project.
         * Dashboard quản trị riêng sẽ triển khai ở Phase 9.
         */
        if (currentUser.getRole()
                == UserRole.ADMIN) {

            return buildAdminDashboard(
                    currentUser,
                    unreadNotifications,
                    today
            );
        }

        long projectCount =
                projectMemberRepository
                        .countByUserId(
                                currentUser.getId()
                        );

        MyTaskSummaryResponse taskSummary =
                buildTaskSummary(
                        currentUser.getId(),
                        today
                );

        MyTaskRiskSummaryResponse riskSummary =
                buildRiskSummary(
                        currentUser.getId(),
                        today
                );

        MyTimeSummaryResponse timeSummary =
                buildTimeSummary(
                        currentUser.getId(),
                        today
                );

        List<MyUpcomingTaskResponse> overdueTasks =
                findDashboardTasks(
                        currentUser.getId(),
                        true,
                        false,
                        today
                );

        List<MyUpcomingTaskResponse> upcomingTasks =
                findDashboardTasks(
                        currentUser.getId(),
                        false,
                        true,
                        today
                );

        return new MyDashboardResponse(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRole(),
                false,
                projectCount,
                unreadNotifications,
                taskSummary,
                riskSummary,
                timeSummary,
                overdueTasks,
                upcomingTasks
        );
    }

    // ===================== MY TASKS =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.MY_TASK_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Ho_Chi_Minh'))" +
                    " + '|keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword())" +
                    " + '|status=' + (#request == null || #request.status() == null ? '' : #request.status())" +
                    " + '|priority=' + (#request == null || #request.priority() == null ? '' : #request.priority())" +
                    " + '|overdue=' + (#request == null || #request.overdueOnly() == null ? '' : #request.overdueOnly())" +
                    " + '|dueSoon=' + (#request == null || #request.dueSoonOnly() == null ? '' : #request.dueSoonOnly())" +
                    " + '|page=' + #pageable.pageNumber" +
                    " + '|size=' + #pageable.pageSize" +
                    " + '|sort=' + #pageable.sort.toString()"
    )
    public MyTaskPageResponse getMyTasks(
            MyTaskSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        if (currentUser.getRole()
                == UserRole.ADMIN) {

            return MyTaskPageResponse.from(
                    Page.empty(pageable)
            );
        }

        MyTaskSearchRequest safeRequest =
                request == null
                        ? emptyTaskRequest()
                        : request;

        validateTaskFilter(
                safeRequest
        );

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        LocalDate dueSoonEndDate =
                today.plusDays(
                        DUE_SOON_DAYS
                );

        Specification<Task> specification =
                Specification.allOf(
                        TaskSpecification
                                .hasAssignee(
                                        currentUser.getId()
                                ),
                        TaskSpecification
                                .search(
                                        safeRequest.keyword()
                                ),
                        TaskSpecification
                                .hasStatus(
                                        safeRequest.status()
                                ),
                        TaskSpecification
                                .hasPriority(
                                        safeRequest.priority()
                                ),
                        TaskSpecification
                                .overdueOnly(
                                        safeRequest.overdueOnly(),
                                        today
                                ),
                        TaskSpecification
                                .dueSoonOnly(
                                        safeRequest.dueSoonOnly(),
                                        today,
                                        dueSoonEndDate
                                )
                );

        Page<Task> taskPage =
                taskRepository.findAll(
                        specification,
                        pageable
                );

        Map<UUID, Project> projectsById =
                loadProjects(
                        taskPage.getContent()
                );

        Page<MyUpcomingTaskResponse> responsePage =
                taskPage.map(task ->
                        toTaskResponse(
                                task,
                                projectsById.get(
                                        task.getProjectId()
                                ),
                                today
                        )
                );

        return MyTaskPageResponse.from(
                responsePage
        );
    }

    // ===================== TIME SUMMARY =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.MY_TIME_SUMMARY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Ho_Chi_Minh'))"
    )
    public MyTimeSummaryResponse getMyTimeSummary() {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        return buildTimeSummary(
                currentUser.getId(),
                today
        );
    }

    // ===================== TASK SUMMARY =====================

    private MyTaskSummaryResponse buildTaskSummary(
            UUID userId,
            LocalDate today
    ) {
        long totalTasks =
                taskRepository
                        .countByAssigneeUserId(
                                userId
                        );

        EnumMap<TaskStatus, Long> counts =
                new EnumMap<>(
                        TaskStatus.class
                );

        for (TaskStatus status
                : TaskStatus.values()) {

            counts.put(
                    status,
                    0L
            );
        }

        List<Object[]> groupedCounts =
                taskRepository
                        .countGroupedByStatusAndAssignee(
                                userId
                        );

        for (Object[] row : groupedCounts) {
            if (row == null
                    || row.length < 2
                    || row[0] == null
                    || row[1] == null) {

                continue;
            }

            TaskStatus status =
                    (TaskStatus) row[0];

            long count =
                    ((Number) row[1])
                            .longValue();

            counts.put(
                    status,
                    count
            );
        }

        long overdueTasks =
                taskRepository
                        .countOverdueByAssignee(
                                userId,
                                today,
                                TERMINAL_STATUSES
                        );

        long dueSoonTasks =
                taskRepository
                        .countDueSoonByAssignee(
                                userId,
                                today,
                                today.plusDays(
                                        DUE_SOON_DAYS
                                ),
                                TERMINAL_STATUSES
                        );

        return new MyTaskSummaryResponse(
                totalTasks,
                counts.get(TaskStatus.TODO),
                counts.get(TaskStatus.IN_PROGRESS),
                counts.get(TaskStatus.IN_REVIEW),
                counts.get(TaskStatus.BLOCKED),
                counts.get(TaskStatus.DONE),
                counts.get(TaskStatus.CANCELLED),
                overdueTasks,
                dueSoonTasks
        );
    }

    // ===================== TIME CALCULATION =====================

    private MyTimeSummaryResponse buildTimeSummary(
            UUID userId,
            LocalDate today
    ) {
        LocalDate weekStartDate =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );

        LocalDate weekEndDate =
                today.with(
                        TemporalAdjusters.nextOrSame(
                                DayOfWeek.SUNDAY
                        )
                );

        LocalDate monthStartDate =
                today.withDayOfMonth(1);

        LocalDate monthEndDate =
                today.with(
                        TemporalAdjusters
                                .lastDayOfMonth()
                );

        long todayMinutes =
                safeLong(
                        taskTimeLogRepository
                                .sumMinutesByUserIdAndWorkDate(
                                        userId,
                                        today
                                )
                );

        long weekMinutes =
                safeLong(
                        taskTimeLogRepository
                                .sumMinutesByUserAndDateRange(
                                        userId,
                                        weekStartDate,
                                        weekEndDate
                                )
                );

        long monthMinutes =
                safeLong(
                        taskTimeLogRepository
                                .sumMinutesByUserAndDateRange(
                                        userId,
                                        monthStartDate,
                                        monthEndDate
                                )
                );

        return new MyTimeSummaryResponse(
                today,
                weekStartDate,
                weekEndDate,
                monthStartDate,
                monthEndDate,
                todayMinutes,
                weekMinutes,
                monthMinutes
        );
    }

    private MyTaskRiskSummaryResponse buildRiskSummary(
            UUID userId,
            LocalDate today
    ) {
        Specification<Task> specification =
                TaskSpecification.hasAssignee(userId);

        List<Task> tasks =
                taskRepository.findAll(specification);

        List<TaskRiskResponse> risks =
                tasks.stream()
                        .map(task -> buildRiskResponse(task, today))
                        .filter(risk ->
                                risk.riskLevel() != TaskRiskLevel.LOW
                                        && risk.riskLevel() != TaskRiskLevel.NONE
                        )
                        .sorted((left, right) ->
                                Integer.compare(
                                        riskWeight(right.riskLevel()),
                                        riskWeight(left.riskLevel())
                                )
                        )
                        .toList();

        return new MyTaskRiskSummaryResponse(
                risks.size(),
                countRiskLevel(risks, TaskRiskLevel.MEDIUM),
                countRiskLevel(risks, TaskRiskLevel.HIGH),
                countRiskLevel(risks, TaskRiskLevel.CRITICAL),
                risks.stream().filter(TaskRiskResponse::overdue).count(),
                risks.stream().filter(TaskRiskResponse::dueSoon).count(),
                risks.stream().filter(TaskRiskResponse::blocked).count(),
                risks.stream().limit(DASHBOARD_TASK_LIMIT).toList()
        );
    }

    private TaskRiskResponse buildRiskResponse(
            Task task,
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

        long unresolvedDependencyCount =
                countUnresolvedDependencies(task);

        long blockingTaskCount =
                taskDependencyRepository
                        .findBlockedTaskIdsByDependency(task.getId())
                        .size();

        TaskRiskLevel riskLevel =
                resolveRiskLevel(
                        overdue,
                        dueSoon,
                        blocked,
                        blockingTaskCount,
                        unresolvedDependencyCount
                );

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

        long daysUntilDue =
                task.getDueDate() == null
                        ? 0L
                        : java.time.temporal.ChronoUnit.DAYS.between(
                                today,
                                task.getDueDate()
                        );

        return new TaskRiskResponse(
                task.getId(),
                task.getProjectId(),
                task.getCurrentSprintId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getAssigneeUserId(),
                null,
                null,
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
                        + task.getProjectId()
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

        if (blockingTaskCount >= 3) {
            return TaskRiskLevel.CRITICAL;
        }

        if (overdue || blocked) {
            return TaskRiskLevel.HIGH;
        }

        if (dueSoon || unresolvedDependencyCount > 0) {
            return TaskRiskLevel.MEDIUM;
        }

        return TaskRiskLevel.LOW;
    }

    private long countUnresolvedDependencies(
            Task task
    ) {
        List<UUID> dependencyIds =
                taskDependencyRepository.findDependsOnTaskIds(task.getId());

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

    private long countRiskLevel(
            List<TaskRiskResponse> risks,
            TaskRiskLevel level
    ) {
        return risks.stream()
                .filter(risk -> risk.riskLevel() == level)
                .count();
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

    // ===================== DASHBOARD TASK LIST =====================

    private List<MyUpcomingTaskResponse>
    findDashboardTasks(
            UUID userId,
            boolean overdueOnly,
            boolean dueSoonOnly,
            LocalDate today
    ) {
        Specification<Task> specification =
                Specification.allOf(
                        TaskSpecification
                                .hasAssignee(userId),
                        TaskSpecification
                                .overdueOnly(
                                        overdueOnly,
                                        today
                                ),
                        TaskSpecification
                                .dueSoonOnly(
                                        dueSoonOnly,
                                        today,
                                        today.plusDays(
                                                DUE_SOON_DAYS
                                        )
                                )
                );

        Pageable pageable =
                PageRequest.of(
                        0,
                        DASHBOARD_TASK_LIMIT,
                        Sort.by(
                                Sort.Direction.ASC,
                                "dueDate"
                        )
                );

        List<Task> tasks =
                taskRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .getContent();

        Map<UUID, Project> projectsById =
                loadProjects(tasks);

        return tasks.stream()
                .map(task ->
                        toTaskResponse(
                                task,
                                projectsById.get(
                                        task.getProjectId()
                                ),
                                today
                        )
                )
                .toList();
    }

    // ===================== PROJECT LOAD =====================

    private Map<UUID, Project> loadProjects(
            List<Task> tasks
    ) {
        if (tasks == null
                || tasks.isEmpty()) {

            return new LinkedHashMap<>();
        }

        List<UUID> projectIds =
                tasks.stream()
                        .map(Task::getProjectId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList();

        if (projectIds.isEmpty()) {
            return new LinkedHashMap<>();
        }

        return projectRepository
                .findAllById(projectIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                Project::getId,
                                Function.identity(),
                                (left, right) -> left,
                                LinkedHashMap::new
                        )
                );
    }

    // ===================== MAPPER =====================

    private MyUpcomingTaskResponse toTaskResponse(
            Task task,
            Project project,
            LocalDate today
    ) {
        LocalDate dueDate =
                task.getDueDate();

        boolean terminal =
                TERMINAL_STATUSES.contains(
                        task.getStatus()
                );

        boolean overdue =
                !terminal
                        && dueDate != null
                        && dueDate.isBefore(today);

        LocalDate dueSoonEndDate =
                today.plusDays(
                        DUE_SOON_DAYS
                );

        boolean dueSoon =
                !terminal
                        && dueDate != null
                        && !dueDate.isBefore(today)
                        && !dueDate.isAfter(
                        dueSoonEndDate
                );

        long daysUntilDue =
                dueDate == null
                        ? 0L
                        : java.time.temporal.ChronoUnit
                          .DAYS
                          .between(
                                  today,
                                  dueDate
                          );

        String targetUrl =
                "/projects/"
                        + task.getProjectId()
                        + "/tasks/"
                        + task.getId();

        return new MyUpcomingTaskResponse(
                task.getId(),
                task.getProjectId(),
                project != null
                        ? project.getCode()
                        : null,
                project != null
                        ? project.getName()
                        : null,
                task.getBacklogItemId(),
                task.getCurrentSprintId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getStartDate(),
                task.getDueDate(),
                overdue,
                dueSoon,
                daysUntilDue,
                targetUrl
        );
    }

    // ===================== ADMIN =====================

    private MyDashboardResponse buildAdminDashboard(
            User currentUser,
            long unreadNotifications,
            LocalDate today
    ) {
        MyTaskSummaryResponse emptyTaskSummary =
                new MyTaskSummaryResponse(
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );

        MyTimeSummaryResponse timeSummary =
                buildTimeSummary(
                        currentUser.getId(),
                        today
                );

        return new MyDashboardResponse(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRole(),
                true,
                0L,
                unreadNotifications,
                emptyTaskSummary,
                new MyTaskRiskSummaryResponse(
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        List.of()
                ),
                timeSummary,
                List.of(),
                List.of()
        );
    }

    // ===================== VALIDATION =====================

    private void validateTaskFilter(
            MyTaskSearchRequest request
    ) {
        if (Boolean.TRUE.equals(
                request.overdueOnly()
        ) && Boolean.TRUE.equals(
                request.dueSoonOnly()
        )) {

            throw new BusinessException(
                    ErrorCode
                            .DASHBOARD_TASK_FILTER_INVALID
            );
        }
    }

    private MyTaskSearchRequest emptyTaskRequest() {
        return new MyTaskSearchRequest(
                null,
                null,
                null,
                null,
                null
        );
    }

    private long safeLong(
            Long value
    ) {
        return value == null
                ? 0L
                : value;
    }
}
