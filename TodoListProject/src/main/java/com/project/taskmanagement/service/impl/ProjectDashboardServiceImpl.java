package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.project.ProjectActivitySearchRequest;
import com.project.taskmanagement.dto.response.dashboard.*;
import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectActivityResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskSummaryResponse;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.ProjectDashboardService;
import com.project.taskmanagement.service.TaskRiskService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectDashboardServiceImpl
        implements ProjectDashboardService {

    ProjectMemberRepository projectMemberRepository;
    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    UserRepository userRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityService projectActivityService;
    TaskRiskService taskRiskService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DEFAULT_ACTIVITY_LIMIT = 10;
    static final int MAX_ACTIVITY_LIMIT = 50;

    static final List<TaskStatus>
            TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    // ===================== DASHBOARD =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_DASHBOARD,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Ho_Chi_Minh'))"
    )
    public ProjectDashboardResponse getDashboard(
            UUID projectId
    ) {
        Project project =
                requireViewAccess(
                        projectId
                );

        List<Task> projectTasks =
                taskRepository
                        .findAllByProjectId(
                                projectId
                        );

        Map<UUID, Long> spentMinutesByTaskId =
                loadSpentMinutesByTaskId(
                        projectTasks
                );

        ProjectDashboardProjectResponse projectResponse =
                buildProjectResponse(
                        project
                );

        ProjectDashboardSprintResponse sprintResponse =
                buildCurrentSprintResponse(
                        projectId
                );

        ProjectDashboardBacklogSummaryResponse backlogResponse =
                buildBacklogSummary(
                        projectId
                );

        ProjectDashboardTaskSummaryResponse taskResponse =
                buildTaskSummary(
                        projectId,
                        projectTasks,
                        spentMinutesByTaskId
                );

        TaskRiskSummaryResponse riskSummary =
                taskRiskService.getProjectRiskSummary(projectId);

        List<ProjectDashboardMemberWorkloadResponse> workload =
                buildWorkload(
                        projectId,
                        projectTasks,
                        spentMinutesByTaskId
                );

        List<ProjectDashboardActivityResponse> activities =
                loadRecentActivities(
                        projectId,
                        DEFAULT_ACTIVITY_LIMIT
                );

        return new ProjectDashboardResponse(
                projectResponse,
                sprintResponse,
                backlogResponse,
                taskResponse,
                riskSummary,
                workload,
                activities
        );
    }

    // ===================== WORKLOAD =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Ho_Chi_Minh'))"
    )
    public List<ProjectDashboardMemberWorkloadResponse>
    getWorkload(
            UUID projectId
    ) {
        requireViewAccess(
                projectId
        );

        List<Task> tasks =
                taskRepository
                        .findAllByProjectId(
                                projectId
                        );

        Map<UUID, Long> spentMinutesByTaskId =
                loadSpentMinutesByTaskId(
                        tasks
                );

        return buildWorkload(
                projectId,
                tasks,
                spentMinutesByTaskId
        );
    }

    // ===================== RECENT ACTIVITY =====================

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDashboardActivityResponse>
    getRecentActivities(
            UUID projectId,
            int limit
    ) {
        requireViewAccess(
                projectId
        );

        int safeLimit =
                normalizeActivityLimit(
                        limit
                );

        return loadRecentActivities(
                projectId,
                safeLimit
        );
    }

    // ===================== ACCESS =====================

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

    // ===================== PROJECT =====================

    private ProjectDashboardProjectResponse
    buildProjectResponse(
            Project project
    ) {
        long memberCount =
                projectMemberRepository
                        .countByProjectId(
                                project.getId()
                        );

        return new ProjectDashboardProjectResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                memberCount
        );
    }

    // ===================== CURRENT SPRINT =====================

    private ProjectDashboardSprintResponse
    buildCurrentSprintResponse(
            UUID projectId
    ) {
        Sprint sprint =
                sprintRepository
                        .findByProjectIdAndStatus(
                                projectId,
                                com.project.taskmanagement.enums
                                        .SprintStatus.ACTIVE
                        )
                        .orElse(null);

        if (sprint == null) {
            return null;
        }

        long backlogItemCount =
                backlogItemRepository
                        .countByProjectIdAndSprintId(
                                projectId,
                                sprint.getId()
                        );

        long taskCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintId(
                                projectId,
                                sprint.getId()
                        );

        long completedTaskCount =
                taskRepository
                        .countByProjectIdAndCurrentSprintIdAndStatus(
                                projectId,
                                sprint.getId(),
                                TaskStatus.DONE
                        );

        return new ProjectDashboardSprintResponse(
                sprint.getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStartedAt(),
                sprint.getCompletedAt(),
                backlogItemCount,
                taskCount,
                completedTaskCount,
                percentage(
                        completedTaskCount,
                        taskCount
                )
        );
    }

    // ===================== BACKLOG =====================

    private ProjectDashboardBacklogSummaryResponse
    buildBacklogSummary(
            UUID projectId
    ) {
        List<BacklogItem> items =
                backlogItemRepository
                        .findAllByProjectIdOrderByPositionAsc(
                                projectId
                        );

        long totalItems =
                items.size();

        long productBacklogItems =
                items.stream()
                        .filter(item ->
                                item.getSprintId() == null
                        )
                        .count();

        long inSprintItems =
                items.stream()
                        .filter(item ->
                                item.getSprintId() != null
                        )
                        .count();

        long draftItems =
                countBacklogStatus(
                        items,
                        BacklogItemStatus.DRAFT
                );

        long readyItems =
                countBacklogStatus(
                        items,
                        BacklogItemStatus.READY
                );

        long doneItems =
                countBacklogStatus(
                        items,
                        BacklogItemStatus.DONE
                );

        long cancelledItems =
                countBacklogStatus(
                        items,
                        BacklogItemStatus.CANCELLED
                );

        long totalStoryPoints =
                items.stream()
                        .map(
                                BacklogItem::getStoryPoints
                        )
                        .filter(value ->
                                value != null
                        )
                        .mapToLong(
                                Integer::longValue
                        )
                        .sum();

        return new ProjectDashboardBacklogSummaryResponse(
                totalItems,
                productBacklogItems,
                inSprintItems,
                draftItems,
                readyItems,
                doneItems,
                cancelledItems,
                totalStoryPoints
        );
    }

    // ===================== TASK SUMMARY =====================

    private ProjectDashboardTaskSummaryResponse
    buildTaskSummary(
            UUID projectId,
            List<Task> tasks,
            Map<UUID, Long> spentMinutesByTaskId
    ) {
        long totalTasks =
                tasks.size();

        long completedTasks =
                countTaskStatus(
                        tasks,
                        TaskStatus.DONE
                );

        long blockedTasks =
                countTaskStatus(
                        tasks,
                        TaskStatus.BLOCKED
                );

        long cancelledTasks =
                countTaskStatus(
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

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        long overdueTasks =
                tasks.stream()
                        .filter(task ->
                                isOverdue(
                                        task,
                                        today
                                )
                        )
                        .count();

        long estimatedMinutes =
                tasks.stream()
                        .map(
                                Task::getEstimatedMinutes
                        )
                        .filter(value ->
                                value != null
                        )
                        .mapToLong(
                                Integer::longValue
                        )
                        .sum();

        long spentMinutes =
                spentMinutesByTaskId
                        .values()
                        .stream()
                        .mapToLong(
                                Long::longValue
                        )
                        .sum();

        long remainingEstimatedMinutes =
                Math.max(
                        estimatedMinutes
                                - spentMinutes,
                        0L
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

        return new ProjectDashboardTaskSummaryResponse(
                totalTasks,
                completedTasks,
                unfinishedTasks,
                blockedTasks,
                cancelledTasks,
                overdueTasks,
                percentage(
                        completedTasks,
                        totalTasks
                ),
                estimatedMinutes,
                spentMinutes,
                remainingEstimatedMinutes,
                percentage(
                        spentMinutes,
                        estimatedMinutes
                ),
                overEstimated,
                overEstimatedMinutes,
                buildStatusSummary(
                        tasks
                )
        );
    }

    private List<ProjectDashboardTaskStatusResponse>
    buildStatusSummary(
            List<Task> tasks
    ) {
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

        for (Task task : tasks) {
            counts.compute(
                    task.getStatus(),
                    (status, currentCount) ->
                            currentCount == null
                                    ? 1L
                                    : currentCount + 1L
            );
        }

        List<ProjectDashboardTaskStatusResponse> result =
                new ArrayList<>();

        for (TaskStatus status
                : TaskStatus.values()) {

            long count =
                    counts.getOrDefault(
                            status,
                            0L
                    );

            result.add(
                    new ProjectDashboardTaskStatusResponse(
                            status,
                            resolveStatusTitle(
                                    status
                            ),
                            count,
                            percentage(
                                    count,
                                    tasks.size()
                            )
                    )
            );
        }

        return result;
    }

    // ===================== WORKLOAD BUILD =====================

    private List<ProjectDashboardMemberWorkloadResponse>
    buildWorkload(
            UUID projectId,
            List<Task> tasks,
            Map<UUID, Long> spentMinutesByTaskId
    ) {
        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        Map<UUID, User> usersById =
                loadUsersByMembers(
                        members
                );

        Map<UUID, MutableMemberWorkload> workloads =
                new LinkedHashMap<>();

        for (ProjectMember member : members) {
            workloads.put(
                    member.getUserId(),
                    new MutableMemberWorkload(
                            member
                    )
            );
        }

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        for (Task task : tasks) {
            UUID assigneeUserId =
                    task.getAssigneeUserId();

            if (assigneeUserId == null) {
                continue;
            }

            MutableMemberWorkload workload =
                    workloads.get(
                            assigneeUserId
                    );

            /*
             * Trường hợp user từng được giao Task nhưng
             * đã không còn là thành viên Project.
             */
            if (workload == null) {
                continue;
            }

            workload.totalTasks++;

            switch (task.getStatus()) {
                case TODO -> workload.todoTasks++;

                case IN_PROGRESS -> workload.inProgressTasks++;

                case IN_REVIEW -> workload.inReviewTasks++;

                case DONE -> workload.doneTasks++;

                case BLOCKED -> workload.blockedTasks++;

                case CANCELLED -> workload.cancelledTasks++;
            }

            if (task.getStatus()
                    != TaskStatus.DONE
                    && task.getStatus()
                    != TaskStatus.CANCELLED) {

                workload.activeTasks++;
            }

            if (isOverdue(
                    task,
                    today
            )) {
                workload.overdueTasks++;
            }

            if (task.getEstimatedMinutes()
                    != null) {

                workload.estimatedMinutes +=
                        task.getEstimatedMinutes();
            }

            workload.spentMinutes +=
                    spentMinutesByTaskId
                            .getOrDefault(
                                    task.getId(),
                                    0L
                            );
        }

        List<ProjectDashboardMemberWorkloadResponse> result =
                new ArrayList<>();

        for (MutableMemberWorkload workload
                : workloads.values()) {

            User user =
                    usersById.get(
                            workload.member
                                    .getUserId()
                    );

            boolean overEstimated =
                    workload.estimatedMinutes > 0
                            && workload.spentMinutes
                            > workload.estimatedMinutes;

            long overEstimatedMinutes =
                    overEstimated
                            ? workload.spentMinutes
                              - workload.estimatedMinutes
                            : 0L;

            result.add(
                    new ProjectDashboardMemberWorkloadResponse(
                            workload.member.getUserId(),
                            user != null
                                    ? user.getUsername()
                                    : "Người dùng không tồn tại",
                            user != null
                                    ? user.getEmail()
                                    : null,
                            user != null
                                    ? user.getRole()
                                    : null,
                            workload.member.getRole(),
                            workload.totalTasks,
                            workload.activeTasks,
                            workload.todoTasks,
                            workload.inProgressTasks,
                            workload.inReviewTasks,
                            workload.doneTasks,
                            workload.blockedTasks,
                            workload.cancelledTasks,
                            workload.overdueTasks,
                            workload.estimatedMinutes,
                            workload.spentMinutes,
                            percentage(
                                    workload.doneTasks,
                                    workload.totalTasks
                            ),
                            overEstimated,
                            overEstimatedMinutes
                    )
            );
        }

        result.sort(
                java.util.Comparator
                        .comparingLong(
                                ProjectDashboardMemberWorkloadResponse
                                        ::activeTasks
                        )
                        .reversed()
                        .thenComparing(
                                response ->
                                        response.username()
                                                == null
                                                ? ""
                                                : response.username()
                        )
        );

        return result;
    }

    // ===================== RECENT ACTIVITIES =====================

    private List<ProjectDashboardActivityResponse>
    loadRecentActivities(
            UUID projectId,
            int limit
    ) {
        ProjectActivityPageResponse pageResponse =
                projectActivityService
                        .getActivities(
                                projectId,
                                new ProjectActivitySearchRequest(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),
                                PageRequest.of(
                                        0,
                                        limit,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        );

        return pageResponse
                .content()
                .stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private ProjectDashboardActivityResponse
    toActivityResponse(
            ProjectActivityResponse activity
    ) {
        return new ProjectDashboardActivityResponse(
                activity.id(),
                activity.entityType(),
                activity.entityId(),
                activity.action(),
                activity.performedByUserId(),
                activity.performedByUsername(),
                activity.displayMessage(),
                activity.createdAt()
        );
    }

    // ===================== TIME LOG =====================

    private Map<UUID, Long>
    loadSpentMinutesByTaskId(
            List<Task> tasks
    ) {
        if (tasks == null
                || tasks.isEmpty()) {

            return new LinkedHashMap<>();
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
                new LinkedHashMap<>();

        for (Object[] row : rows) {
            if (row == null
                    || row.length < 2
                    || row[0] == null) {

                continue;
            }

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
                        .map(
                                ProjectMember::getUserId
                        )
                        .distinct()
                        .toList();

        return userRepository
                .findAllById(
                        userIds
                )
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

    private long countTaskStatus(
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

    private long countBacklogStatus(
            List<BacklogItem> items,
            BacklogItemStatus status
    ) {
        return items.stream()
                .filter(item ->
                        item.getStatus()
                                == status
                )
                .count();
    }

    private boolean isOverdue(
            Task task,
            LocalDate today
    ) {
        return task.getDueDate() != null
                && task.getDueDate()
                .isBefore(today)
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

    private int normalizeActivityLimit(
            int limit
    ) {
        if (limit <= 0) {
            return DEFAULT_ACTIVITY_LIMIT;
        }

        return Math.min(
                limit,
                MAX_ACTIVITY_LIMIT
        );
    }

    // ===================== INTERNAL MODEL =====================

    private static class MutableMemberWorkload {

        final ProjectMember member;

        long totalTasks;
        long activeTasks;

        long todoTasks;
        long inProgressTasks;
        long inReviewTasks;
        long doneTasks;
        long blockedTasks;
        long cancelledTasks;

        long overdueTasks;

        long estimatedMinutes;
        long spentMinutes;

        MutableMemberWorkload(
                ProjectMember member
        ) {
            this.member = member;
        }
    }
}
