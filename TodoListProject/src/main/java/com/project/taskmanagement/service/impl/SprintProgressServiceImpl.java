package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.sprint.SprintProgressDailyResponse;
import com.project.taskmanagement.dto.response.sprint.SprintProgressResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReminderResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.SprintProgressService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintProgressServiceImpl
        implements SprintProgressService {

    SprintRepository sprintRepository;
    TaskRepository taskRepository;
    ProjectMemberRepository projectMemberRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    NotificationService notificationService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final long ENDING_SOON_DAYS = 2L;

    static final double ALLOWED_PROGRESS_GAP = 10.0;

    static final List<TaskStatus> TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    // ===================== PROGRESS =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_PROGRESS,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintProgressResponse getProgress(
            UUID projectId,
            UUID sprintId
    ) {
        requireViewAccess(
                projectId
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

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        LocalDate startDate =
                resolveStartDate(
                        sprint
                );

        LocalDate endDate =
                resolveEndDate(
                        sprint,
                        startDate,
                        today
                );

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

        long unfinishedTasks =
                tasks.stream()
                        .filter(task ->
                                !TERMINAL_STATUSES.contains(
                                        task.getStatus()
                                )
                        )
                        .count();

        long overdueTasks =
                tasks.stream()
                        .filter(task ->
                                isTaskOverdue(
                                        task,
                                        today
                                )
                        )
                        .count();

        long totalDays =
                Math.max(
                        ChronoUnit.DAYS.between(
                                startDate,
                                endDate
                        ) + 1L,
                        1L
                );

        long elapsedDays =
                calculateElapsedDays(
                        startDate,
                        endDate,
                        today
                );

        long daysRemaining =
                ChronoUnit.DAYS.between(
                        today,
                        endDate
                );

        double actualCompletionRate =
                percentage(
                        completedTasks,
                        totalTasks
                );

        double expectedProgressRate =
                percentage(
                        elapsedDays,
                        totalDays
                );

        double progressGap =
                roundTwoDecimals(
                        expectedProgressRate
                                - actualCompletionRate
                );

        boolean behindSchedule =
                progressGap > ALLOWED_PROGRESS_GAP;

        boolean endingSoon =
                sprint.getStatus() == SprintStatus.ACTIVE
                        && daysRemaining >= 0
                        && daysRemaining <= ENDING_SOON_DAYS;

        boolean overdueSprint =
                daysRemaining < 0
                        && sprint.getStatus() == SprintStatus.ACTIVE;

        return new SprintProgressResponse(
                projectId,
                sprint.getId(),
                sprint.getName(),
                sprint.getStatus(),
                startDate,
                endDate,
                today,
                totalDays,
                elapsedDays,
                daysRemaining,
                totalTasks,
                completedTasks,
                unfinishedTasks,
                blockedTasks,
                overdueTasks,
                actualCompletionRate,
                expectedProgressRate,
                progressGap,
                behindSchedule,
                endingSoon,
                overdueSprint,
                buildDailyProgress(
                        tasks,
                        startDate,
                        endDate,
                        totalTasks
                )
        );
    }

    // ===================== REMIND ENDING =====================

    @Override
    @Transactional
    public SprintReminderResponse remindEndingSoon(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                requireSprintManagementAccess(
                        projectId
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        if (sprint.getStatus() != SprintStatus.ACTIVE
                || sprint.getEndDate() == null) {

            return emptyReminder(
                    projectId,
                    sprintId,
                    "SPRINT_ENDING_SOON"
            );
        }

        long daysRemaining =
                ChronoUnit.DAYS.between(
                        today,
                        sprint.getEndDate()
                );

        if (daysRemaining < 0
                || daysRemaining > ENDING_SOON_DAYS) {

            return emptyReminder(
                    projectId,
                    sprintId,
                    "SPRINT_ENDING_SOON"
            );
        }

        List<Task> unfinishedTasks =
                taskRepository.findUnfinishedBySprint(
                        projectId,
                        sprintId,
                        TERMINAL_STATUSES
                );

        LinkedHashSet<UUID> recipientIds =
                new LinkedHashSet<>(
                        findProjectManagerRecipientIds(
                                projectId
                        )
                );

        unfinishedTasks.stream()
                .map(Task::getAssigneeUserId)
                .filter(id -> id != null)
                .forEach(recipientIds::add);

        recipientIds.remove(
                currentUser.getId()
        );

        sendNotificationIfHasRecipients(
                NotificationType.SPRINT_ENDING_SOON,
                "Sprint sắp kết thúc",
                "Sprint "
                        + sprint.getName()
                        + " còn "
                        + daysRemaining
                        + " ngày để hoàn thành.",
                currentUser.getId(),
                projectId,
                sprint.getId(),
                recipientIds
        );

        return reminderResponse(
                projectId,
                sprintId,
                "SPRINT_ENDING_SOON",
                unfinishedTasks.size(),
                recipientIds
        );
    }

    // ===================== REMIND OVERDUE =====================

    @Override
    @Transactional
    public SprintReminderResponse remindOverdueTasks(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                requireSprintManagementAccess(
                        projectId
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        LocalDate today =
                LocalDate.now(
                        BUSINESS_ZONE
                );

        List<Task> overdueTasks =
                taskRepository.findOverdueBySprint(
                        projectId,
                        sprintId,
                        today,
                        TERMINAL_STATUSES
                );

        LinkedHashSet<UUID> recipientIds =
                new LinkedHashSet<>();

        overdueTasks.stream()
                .map(Task::getAssigneeUserId)
                .filter(id -> id != null)
                .forEach(recipientIds::add);

        recipientIds.remove(
                currentUser.getId()
        );

        sendNotificationIfHasRecipients(
                NotificationType.TASK_OVERDUE,
                "Có Task quá hạn trong Sprint",
                "Sprint "
                        + sprint.getName()
                        + " có "
                        + overdueTasks.size()
                        + " Task quá hạn.",
                currentUser.getId(),
                projectId,
                sprint.getId(),
                recipientIds
        );

        return reminderResponse(
                projectId,
                sprintId,
                "TASK_OVERDUE",
                overdueTasks.size(),
                recipientIds
        );
    }

    // ===================== REMIND BLOCKED =====================

    @Override
    @Transactional
    public SprintReminderResponse remindBlockedTasks(
            UUID projectId,
            UUID sprintId
    ) {
        User currentUser =
                requireSprintManagementAccess(
                        projectId
                );

        Sprint sprint =
                getSprintOrThrow(
                        projectId,
                        sprintId
                );

        List<Task> blockedTasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintIdAndStatusOrderByPositionAsc(
                                projectId,
                                sprintId,
                                TaskStatus.BLOCKED
                        );

        LinkedHashSet<UUID> recipientIds =
                new LinkedHashSet<>(
                        findProjectManagerRecipientIds(
                                projectId
                        )
                );

        blockedTasks.stream()
                .map(Task::getAssigneeUserId)
                .filter(id -> id != null)
                .forEach(recipientIds::add);

        recipientIds.remove(
                currentUser.getId()
        );

        sendNotificationIfHasRecipients(
                NotificationType.TASK_BLOCKED,
                "Có Task đang bị chặn",
                "Sprint "
                        + sprint.getName()
                        + " có "
                        + blockedTasks.size()
                        + " Task đang bị chặn.",
                currentUser.getId(),
                projectId,
                sprint.getId(),
                recipientIds
        );

        return reminderResponse(
                projectId,
                sprintId,
                "TASK_BLOCKED",
                blockedTasks.size(),
                recipientIds
        );
    }

    // ===================== ACCESS =====================

    private void requireViewAccess(
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
    }

    private User requireSprintManagementAccess(
            UUID projectId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireSprintManagementAccess(
                        projectId,
                        currentUser
                );

        return currentUser;
    }

    // ===================== QUERY HELPER =====================

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

    private List<UUID> findProjectManagerRecipientIds(
            UUID projectId
    ) {
        List<ProjectMember> owners =
                projectMemberRepository
                        .findAllByProjectIdAndRole(
                                projectId,
                                ProjectMemberRole.OWNER
                        );

        List<ProjectMember> managers =
                projectMemberRepository
                        .findAllByProjectIdAndRole(
                                projectId,
                                ProjectMemberRole.PROJECT_MANAGER
                        );

        LinkedHashSet<UUID> result =
                new LinkedHashSet<>();

        owners.stream()
                .map(ProjectMember::getUserId)
                .forEach(result::add);

        managers.stream()
                .map(ProjectMember::getUserId)
                .forEach(result::add);

        return List.copyOf(result);
    }

    // ===================== DATE =====================

    private LocalDate resolveStartDate(
            Sprint sprint
    ) {
        if (sprint.getStartDate() != null) {
            return sprint.getStartDate();
        }

        if (sprint.getStartedAt() != null) {
            return sprint.getStartedAt()
                    .atZone(BUSINESS_ZONE)
                    .toLocalDate();
        }

        if (sprint.getCreatedAt() != null) {
            return sprint.getCreatedAt()
                    .atZone(BUSINESS_ZONE)
                    .toLocalDate();
        }

        return LocalDate.now(BUSINESS_ZONE);
    }

    private LocalDate resolveEndDate(
            Sprint sprint,
            LocalDate startDate,
            LocalDate today
    ) {
        LocalDate endDate =
                sprint.getEndDate() != null
                        ? sprint.getEndDate()
                        : today;

        if (endDate.isBefore(startDate)) {
            return startDate;
        }

        return endDate;
    }

    private long calculateElapsedDays(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today
    ) {
        if (today.isBefore(startDate)) {
            return 0L;
        }

        LocalDate cappedToday =
                today.isAfter(endDate)
                        ? endDate
                        : today;

        return ChronoUnit.DAYS.between(
                startDate,
                cappedToday
        ) + 1L;
    }

    // ===================== DAILY PROGRESS =====================

    private List<SprintProgressDailyResponse>
    buildDailyProgress(
            List<Task> tasks,
            LocalDate startDate,
            LocalDate endDate,
            long totalTasks
    ) {
        Map<LocalDate, Long> completedByDate =
                tasks.stream()
                        .filter(task ->
                                task.getStatus() == TaskStatus.DONE
                                        && task.getCompletedAt() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        task -> task.getCompletedAt()
                                                .atZone(BUSINESS_ZONE)
                                                .toLocalDate(),
                                        Collectors.counting()
                                )
                        );

        long totalDays =
                Math.max(
                        ChronoUnit.DAYS.between(
                                startDate,
                                endDate
                        ) + 1L,
                        1L
                );

        List<SprintProgressDailyResponse> result =
                new ArrayList<>();

        long cumulativeCompleted = 0L;

        for (long dayIndex = 0;
             dayIndex < totalDays;
             dayIndex++) {

            LocalDate date =
                    startDate.plusDays(
                            dayIndex
                    );

            long completedOnDate =
                    completedByDate.getOrDefault(
                            date,
                            0L
                    );

            cumulativeCompleted += completedOnDate;

            long remainingTasks =
                    Math.max(
                            totalTasks
                                    - cumulativeCompleted,
                            0L
                    );

            double idealRemainingTasks =
                    totalDays <= 1
                            ? 0.0
                            : totalTasks
                            * (1.0
                            - ((double) dayIndex
                            / (totalDays - 1)));

            result.add(
                    new SprintProgressDailyResponse(
                            date,
                            completedOnDate,
                            cumulativeCompleted,
                            remainingTasks,
                            roundTwoDecimals(
                                    Math.max(
                                            idealRemainingTasks,
                                            0.0
                                    )
                            )
                    )
            );
        }

        return result;
    }

    // ===================== NOTIFICATION HELPER =====================

    private void sendNotificationIfHasRecipients(
            NotificationType type,
            String title,
            String content,
            UUID actorUserId,
            UUID projectId,
            UUID sprintId,
            LinkedHashSet<UUID> recipientIds
    ) {
        if (recipientIds.isEmpty()) {
            return;
        }

        notificationService.create(
                new NotificationCommand(
                        type,
                        title,
                        content,
                        actorUserId,
                        projectId,
                        ActivityEntityType.SPRINT,
                        sprintId,
                        List.copyOf(recipientIds)
                )
        );
    }

    private SprintReminderResponse emptyReminder(
            UUID projectId,
            UUID sprintId,
            String reminderType
    ) {
        return new SprintReminderResponse(
                projectId,
                sprintId,
                reminderType,
                0L,
                0L,
                List.of(),
                Instant.now()
        );
    }

    private SprintReminderResponse reminderResponse(
            UUID projectId,
            UUID sprintId,
            String reminderType,
            long targetItemCount,
            LinkedHashSet<UUID> recipientIds
    ) {
        return new SprintReminderResponse(
                projectId,
                sprintId,
                reminderType,
                targetItemCount,
                recipientIds.size(),
                List.copyOf(recipientIds),
                Instant.now()
        );
    }

    // ===================== GENERAL HELPER =====================

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

    private boolean isTaskOverdue(
            Task task,
            LocalDate today
    ) {
        return task.getDueDate() != null
                && task.getDueDate().isBefore(today)
                && !TERMINAL_STATUSES
                .contains(task.getStatus());
    }

    private double percentage(
            long value,
            long total
    ) {
        if (total <= 0L) {
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
}
