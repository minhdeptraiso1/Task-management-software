package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.analytics.BurnupChartResponse;
import com.project.taskmanagement.dto.response.analytics.BurnupPointResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowPointResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowResponse;
import com.project.taskmanagement.dto.response.analytics.ProjectAnalyticsSummaryResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityPointResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.service.AnalyticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    static final List<TaskStatus> EXCLUDED_OVERDUE_STATUSES = List.of(TaskStatus.DONE, TaskStatus.CANCELLED);

    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    @Override
    @Cacheable(
            value = CacheNames.ANALYTICS_VELOCITY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() + '|project=' + #projectId"
    )
    @Transactional(readOnly = true)
    public VelocityChartResponse getVelocity(UUID projectId) {
        Project project = requireProjectViewAccess(projectId);
        List<Sprint> targetSprints = sprintRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .filter(sprint -> sprint.getStatus() == SprintStatus.COMPLETED || sprint.getStatus() == SprintStatus.ACTIVE)
                .toList()
                .reversed();

        List<VelocityPointResponse> points = new ArrayList<>();
        long totalCompletedItems = 0;
        long totalCompletedStoryPoints = 0;

        for (Sprint sprint : targetSprints) {
            long committedItems = backlogItemRepository.countByProjectIdAndSprintId(projectId, sprint.getId());
            long completedItems = backlogItemRepository.countByProjectIdAndSprintIdAndStatus(
                    projectId,
                    sprint.getId(),
                    BacklogItemStatus.DONE
            );
            long committedStoryPoints = safeLong(backlogItemRepository.sumStoryPointsBySprint(projectId, sprint.getId()));
            long completedStoryPoints = safeLong(backlogItemRepository.sumStoryPointsBySprintAndStatus(
                    projectId,
                    sprint.getId(),
                    BacklogItemStatus.DONE
            ));

            totalCompletedItems += completedItems;
            totalCompletedStoryPoints += completedStoryPoints;

            points.add(new VelocityPointResponse(
                    sprint.getId(),
                    sprint.getName(),
                    sprint.getStatus(),
                    sprint.getStartDate(),
                    sprint.getEndDate(),
                    committedItems,
                    completedItems,
                    committedStoryPoints,
                    completedStoryPoints,
                    percent(completedItems, committedItems)
            ));
        }

        long sprintCount = points.size();
        return new VelocityChartResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                sprintCount == 0 ? 0 : Math.round((double) totalCompletedItems / sprintCount),
                sprintCount == 0 ? 0 : Math.round((double) totalCompletedStoryPoints / sprintCount),
                points
        );
    }

    @Override
    @Cacheable(
            value = CacheNames.ANALYTICS_PROJECT_BURNUP,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() + '|project=' + #projectId"
    )
    @Transactional(readOnly = true)
    public BurnupChartResponse getProjectBurnup(UUID projectId) {
        Project project = requireProjectViewAccess(projectId);
        LocalDate startDate = project.getStartDate() == null ? LocalDate.now(BUSINESS_ZONE) : project.getStartDate();
        LocalDate endDate = project.getEndDate() == null ? LocalDate.now(BUSINESS_ZONE) : project.getEndDate();
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        List<Task> tasks = taskRepository.findAllByProjectId(projectId);
        List<BurnupPointResponse> points = buildBurnupPoints(startDate, endDate, tasks.size(), tasks);

        return new BurnupChartResponse(projectId, null, null, startDate, endDate, points);
    }

    @Override
    @Cacheable(
            value = CacheNames.ANALYTICS_SPRINT_BURNUP,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() + '|project=' + #projectId + '|sprint=' + #sprintId"
    )
    @Transactional(readOnly = true)
    public BurnupChartResponse getSprintBurnup(UUID projectId, UUID sprintId) {
        requireProjectViewAccess(projectId);
        Sprint sprint = getSprintOrThrow(projectId, sprintId);
        LocalDate startDate = sprint.getStartDate() == null ? LocalDate.now(BUSINESS_ZONE) : sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate() == null ? startDate : sprint.getEndDate();
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        List<Task> tasks = taskRepository.findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(projectId, sprintId);
        List<BurnupPointResponse> points = buildBurnupPoints(startDate, endDate, tasks.size(), tasks);

        return new BurnupChartResponse(projectId, sprintId, sprint.getName(), startDate, endDate, points);
    }

    @Override
    @Cacheable(
            value = CacheNames.ANALYTICS_CUMULATIVE_FLOW,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() + '|project=' + #projectId + '|sprint=' + #sprintId"
    )
    @Transactional(readOnly = true)
    public CumulativeFlowResponse getSprintCumulativeFlow(UUID projectId, UUID sprintId) {
        requireProjectViewAccess(projectId);
        Sprint sprint = getSprintOrThrow(projectId, sprintId);
        LocalDate startDate = sprint.getStartDate() == null ? LocalDate.now(BUSINESS_ZONE) : sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate() == null ? startDate : sprint.getEndDate();
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        List<Task> tasks = taskRepository.findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(projectId, sprintId);
        List<CumulativeFlowPointResponse> points = new ArrayList<>();

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            points.add(new CumulativeFlowPointResponse(
                    cursor,
                    countCurrentStatus(tasks, TaskStatus.TODO),
                    countCurrentStatus(tasks, TaskStatus.IN_PROGRESS),
                    countCurrentStatus(tasks, TaskStatus.IN_REVIEW),
                    countCurrentStatus(tasks, TaskStatus.BLOCKED),
                    countDoneUntil(tasks, cursor),
                    countCurrentStatus(tasks, TaskStatus.CANCELLED)
            ));
            cursor = cursor.plusDays(1);
        }

        return new CumulativeFlowResponse(projectId, sprintId, sprint.getName(), points);
    }

    @Override
    @Cacheable(
            value = CacheNames.ANALYTICS_SUMMARY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() + '|project=' + #projectId"
    )
    @Transactional(readOnly = true)
    public ProjectAnalyticsSummaryResponse getProjectAnalyticsSummary(UUID projectId) {
        Project project = requireProjectViewAccess(projectId);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        long totalSprints = sprintRepository.countByProjectId(projectId);
        long completedSprints = sprintRepository.countByProjectIdAndStatus(projectId, SprintStatus.COMPLETED);
        long activeSprints = sprintRepository.countByProjectIdAndStatus(projectId, SprintStatus.ACTIVE);
        long totalBacklogItems = backlogItemRepository.countByProjectId(projectId);
        long completedBacklogItems = backlogItemRepository.countByProjectIdAndStatus(projectId, BacklogItemStatus.DONE);
        long totalTasks = taskRepository.countByProjectId(projectId);
        long completedTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        long blockedTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.BLOCKED);
        long overdueTasks = taskRepository.countOverdueByProjectId(projectId, today, EXCLUDED_OVERDUE_STATUSES);
        long estimatedMinutes = safeLong(taskRepository.sumEstimatedMinutesByProjectId(projectId));
        long spentMinutes = safeLong(taskTimeLogRepository.sumProjectMinutes(projectId));

        return new ProjectAnalyticsSummaryResponse(
                projectId,
                project.getCode(),
                project.getName(),
                totalSprints,
                completedSprints,
                activeSprints,
                totalBacklogItems,
                completedBacklogItems,
                totalTasks,
                completedTasks,
                blockedTasks,
                overdueTasks,
                estimatedMinutes,
                spentMinutes,
                percent(completedTasks, totalTasks),
                percent(completedBacklogItems, totalBacklogItems)
        );
    }

    private Project requireProjectViewAccess(UUID projectId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        return project;
    }

    private Sprint getSprintOrThrow(UUID projectId, UUID sprintId) {
        return sprintRepository.findByIdAndProjectId(sprintId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPRINT_NOT_FOUND));
    }

    private List<BurnupPointResponse> buildBurnupPoints(
            LocalDate startDate,
            LocalDate endDate,
            long totalScope,
            List<Task> tasks
    ) {
        List<BurnupPointResponse> points = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            long completedScope = countDoneUntil(tasks, cursor);
            points.add(new BurnupPointResponse(
                    cursor,
                    totalScope,
                    completedScope,
                    percent(completedScope, totalScope)
            ));
            cursor = cursor.plusDays(1);
        }
        return points;
    }

    private long countDoneUntil(List<Task> tasks, LocalDate date) {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .filter(task -> task.getCompletedAt() != null)
                .filter(task -> !task.getCompletedAt()
                        .atZone(BUSINESS_ZONE)
                        .toLocalDate()
                        .isAfter(date))
                .count();
    }

    private long countCurrentStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private double percent(long value, long total) {
        if (total <= 0) {
            return 0D;
        }
        return Math.round(value * 10000D / total) / 100D;
    }
}
