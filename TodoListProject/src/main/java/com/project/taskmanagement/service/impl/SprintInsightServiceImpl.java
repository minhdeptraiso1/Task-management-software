package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.sprint.SprintCapacityMemberResponse;
import com.project.taskmanagement.dto.response.sprint.SprintCapacityResponse;
import com.project.taskmanagement.dto.response.sprint.SprintHealthResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRiskResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SprintHealthStatus;
import com.project.taskmanagement.enums.SprintRiskSeverity;
import com.project.taskmanagement.enums.SprintRiskType;
import com.project.taskmanagement.enums.SprintStatus;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.SprintInsightService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintInsightServiceImpl
        implements SprintInsightService {

    SprintRepository sprintRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final long DAILY_CAPACITY_MINUTES = 480L;

    static final List<TaskStatus> TERMINAL_STATUSES =
            List.of(
                    TaskStatus.DONE,
                    TaskStatus.CANCELLED
            );

    // ===================== CAPACITY =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_CAPACITY,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintCapacityResponse getCapacity(
            UUID projectId,
            UUID sprintId
    ) {
        Sprint sprint =
                getSprintWithAccess(
                        projectId,
                        sprintId
                );

        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        Map<UUID, User> usersById =
                loadUsersByMembers(members);

        Map<UUID, Long> estimatedByAssignee =
                loadEstimatedByAssignee(
                        projectId,
                        sprintId
                );

        Map<UUID, Long> spentByUser =
                loadSpentByUser(
                        projectId,
                        sprintId
                );

        Map<UUID, EnumMap<TaskStatus, Long>> statusCountByUser =
                loadStatusCountByUser(
                        projectId,
                        sprintId
                );

        long sprintDays =
                calculateSprintDays(sprint);

        long memberCapacity =
                sprintDays * DAILY_CAPACITY_MINUTES;

        List<SprintCapacityMemberResponse> memberResponses =
                new ArrayList<>();

        long totalCapacityMinutes = 0L;
        long totalEstimatedMinutes = 0L;
        long totalSpentMinutes = 0L;

        for (ProjectMember member : members) {
            UUID userId =
                    member.getUserId();

            User user =
                    usersById.get(userId);

            long assignedEstimatedMinutes =
                    estimatedByAssignee.getOrDefault(
                            userId,
                            0L
                    );

            long spentMinutes =
                    spentByUser.getOrDefault(
                            userId,
                            0L
                    );

            EnumMap<TaskStatus, Long> counts =
                    statusCountByUser.getOrDefault(
                            userId,
                            emptyStatusCounts()
                    );

            long remainingCapacityMinutes =
                    Math.max(
                            memberCapacity
                                    - assignedEstimatedMinutes,
                            0L
                    );

            long overCapacityMinutes =
                    Math.max(
                            assignedEstimatedMinutes
                                    - memberCapacity,
                            0L
                    );

            long totalTasks =
                    counts.values()
                            .stream()
                            .mapToLong(Long::longValue)
                            .sum();

            totalCapacityMinutes += memberCapacity;
            totalEstimatedMinutes += assignedEstimatedMinutes;
            totalSpentMinutes += spentMinutes;

            memberResponses.add(
                    new SprintCapacityMemberResponse(
                            userId,
                            user != null
                                    ? user.getUsername()
                                    : null,
                            user != null
                                    ? user.getEmail()
                                    : null,
                            user != null
                                    ? user.getRole()
                                    : null,
                            member.getRole(),
                            memberCapacity,
                            assignedEstimatedMinutes,
                            spentMinutes,
                            remainingCapacityMinutes,
                            percentage(
                                    assignedEstimatedMinutes,
                                    memberCapacity
                            ),
                            overCapacityMinutes > 0,
                            overCapacityMinutes,
                            totalTasks,
                            counts.get(TaskStatus.TODO),
                            counts.get(TaskStatus.IN_PROGRESS),
                            counts.get(TaskStatus.IN_REVIEW),
                            counts.get(TaskStatus.DONE),
                            counts.get(TaskStatus.BLOCKED),
                            counts.get(TaskStatus.CANCELLED)
                    )
            );
        }

        long remainingCapacityMinutes =
                Math.max(
                        totalCapacityMinutes
                                - totalEstimatedMinutes,
                        0L
                );

        long overCapacityMinutes =
                Math.max(
                        totalEstimatedMinutes
                                - totalCapacityMinutes,
                        0L
                );

        return new SprintCapacityResponse(
                projectId,
                sprintId,
                sprint.getName(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprintDays,
                members.size(),
                totalCapacityMinutes,
                totalEstimatedMinutes,
                totalSpentMinutes,
                remainingCapacityMinutes,
                percentage(
                        totalEstimatedMinutes,
                        totalCapacityMinutes
                ),
                overCapacityMinutes > 0,
                overCapacityMinutes,
                memberResponses
        );
    }

    // ===================== HEALTH =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_HEALTH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public SprintHealthResponse getHealth(
            UUID projectId,
            UUID sprintId
    ) {
        Sprint sprint =
                getSprintWithAccess(
                        projectId,
                        sprintId
                );

        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintId(
                                projectId,
                                sprintId
                        );

        SprintCapacityResponse capacity =
                buildCapacityWithoutAccessCheck(
                        projectId,
                        sprint
                );

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

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

        long overdueTasks =
                tasks.stream()
                        .filter(task ->
                                isOverdue(task, today)
                        )
                        .count();

        long noAssigneeTasks =
                taskRepository
                        .countUnassignedActiveTasksInSprint(
                                projectId,
                                sprintId,
                                TERMINAL_STATUSES
                        );

        long noEstimateTasks =
                taskRepository
                        .countNoEstimateActiveTasksInSprint(
                                projectId,
                                sprintId,
                                TERMINAL_STATUSES
                        );

        long totalDays =
                calculateSprintDays(sprint);

        long elapsedDays =
                calculateElapsedDays(
                        sprint,
                        today
                );

        long remainingDays =
                calculateRemainingDays(
                        sprint,
                        today
                );

        double progressRate =
                percentage(
                        completedTasks,
                        totalTasks
                );

        double expectedProgressRate =
                percentage(
                        elapsedDays,
                        totalDays
                );

        double timeUsageRate =
                percentage(
                        capacity.totalSpentMinutes(),
                        capacity.totalEstimatedMinutes()
                );

        List<SprintRiskResponse> risks =
                buildRisks(
                        projectId,
                        sprint,
                        tasks,
                        capacity,
                        today,
                        progressRate,
                        expectedProgressRate
                );

        long criticalRiskCount =
                risks.stream()
                        .filter(risk ->
                                risk.severity()
                                        == SprintRiskSeverity.CRITICAL
                        )
                        .count();

        SprintHealthStatus healthStatus =
                resolveHealthStatus(
                        risks,
                        progressRate,
                        expectedProgressRate
                );

        return new SprintHealthResponse(
                projectId,
                sprintId,
                sprint.getName(),
                sprint.getStatus(),
                healthStatus,
                sprint.getStartDate(),
                sprint.getEndDate(),
                totalDays,
                elapsedDays,
                remainingDays,
                totalTasks,
                completedTasks,
                totalTasks - completedTasks,
                blockedTasks,
                overdueTasks,
                noAssigneeTasks,
                noEstimateTasks,
                progressRate,
                expectedProgressRate,
                timeUsageRate,
                capacity.overCapacity(),
                capacity.totalCapacityMinutes(),
                capacity.totalEstimatedMinutes(),
                capacity.totalSpentMinutes(),
                risks.size(),
                criticalRiskCount,
                risks.stream()
                        .limit(5)
                        .toList()
        );
    }

    // ===================== RISKS =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.SPRINT_RISKS,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|project=' + #projectId" +
                    " + '|sprint=' + #sprintId"
    )
    public List<SprintRiskResponse> getRisks(
            UUID projectId,
            UUID sprintId
    ) {
        Sprint sprint =
                getSprintWithAccess(
                        projectId,
                        sprintId
                );

        List<Task> tasks =
                taskRepository
                        .findAllByProjectIdAndCurrentSprintId(
                                projectId,
                                sprintId
                        );

        SprintCapacityResponse capacity =
                buildCapacityWithoutAccessCheck(
                        projectId,
                        sprint
                );

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        long totalTasks =
                tasks.size();

        long completedTasks =
                countStatus(
                        tasks,
                        TaskStatus.DONE
                );

        double progressRate =
                percentage(
                        completedTasks,
                        totalTasks
                );

        double expectedProgressRate =
                percentage(
                        calculateElapsedDays(
                                sprint,
                                today
                        ),
                        calculateSprintDays(sprint)
                );

        return buildRisks(
                projectId,
                sprint,
                tasks,
                capacity,
                today,
                progressRate,
                expectedProgressRate
        );
    }

    private List<SprintRiskResponse> buildRisks(
            UUID projectId,
            Sprint sprint,
            List<Task> tasks,
            SprintCapacityResponse capacity,
            LocalDate today,
            double progressRate,
            double expectedProgressRate
    ) {
        List<SprintRiskResponse> risks =
                new ArrayList<>();

        if (capacity.overCapacity()) {
            risks.add(
                    new SprintRiskResponse(
                            SprintRiskType.OVER_CAPACITY,
                            SprintRiskSeverity.CRITICAL,
                            "Sprint vượt capacity "
                                    + capacity.overCapacityMinutes()
                                    + " phút",
                            null,
                            null,
                            null,
                            null,
                            "Giảm scope hoặc bổ sung nhân sự trước khi tiếp tục Sprint"
                    )
            );
        }

        for (SprintCapacityMemberResponse member :
                capacity.members()) {

            if (member.overCapacity()) {
                risks.add(
                        new SprintRiskResponse(
                                SprintRiskType.OVER_CAPACITY,
                                SprintRiskSeverity.HIGH,
                                "Thành viên "
                                        + member.username()
                                        + " vượt capacity "
                                        + member.overCapacityMinutes()
                                        + " phút",
                                null,
                                null,
                                member.userId(),
                                member.username(),
                                "Phân bổ lại task hoặc giảm khối lượng cho thành viên này"
                        )
                );
            }
        }

        if (progressRate + 20.0 < expectedProgressRate) {
            risks.add(
                    new SprintRiskResponse(
                            SprintRiskType.LOW_PROGRESS,
                            SprintRiskSeverity.HIGH,
                            "Tiến độ thấp hơn kỳ vọng nhiều",
                            null,
                            null,
                            null,
                            null,
                            "Rà soát scope, blocker và ưu tiên task quan trọng"
                    )
            );
        } else if (progressRate + 10.0 < expectedProgressRate) {
            risks.add(
                    new SprintRiskResponse(
                            SprintRiskType.LOW_PROGRESS,
                            SprintRiskSeverity.MEDIUM,
                            "Tiến độ thấp hơn kỳ vọng",
                            null,
                            null,
                            null,
                            null,
                            "Theo dõi sát daily progress và cập nhật kế hoạch"
                    )
            );
        }

        long remainingDays =
                calculateRemainingDays(
                        sprint,
                        today
                );

        if (sprint.getEndDate() != null
                && today.isAfter(sprint.getEndDate())
                && sprint.getStatus()
                != SprintStatus.COMPLETED) {

            risks.add(
                    new SprintRiskResponse(
                            SprintRiskType.SPRINT_OVERDUE,
                            SprintRiskSeverity.CRITICAL,
                            "Sprint đã quá hạn nhưng chưa hoàn thành",
                            null,
                            null,
                            null,
                            null,
                            "Hoàn tất Sprint hoặc chuyển phần chưa xong về Product Backlog"
                    )
            );
        } else if (sprint.getStatus() == SprintStatus.ACTIVE
                && sprint.getEndDate() != null
                && remainingDays <= 2) {

            risks.add(
                    new SprintRiskResponse(
                            SprintRiskType.SPRINT_ENDING_SOON,
                            SprintRiskSeverity.MEDIUM,
                            "Sprint sắp kết thúc trong "
                                    + remainingDays
                                    + " ngày",
                            null,
                            null,
                            null,
                            null,
                            "Ưu tiên hoàn tất task quan trọng và chuẩn bị review"
                    )
            );
        }

        for (Task task : tasks) {
            if (isOverdue(task, today)) {
                risks.add(
                        new SprintRiskResponse(
                                SprintRiskType.OVERDUE_TASK,
                                SprintRiskSeverity.HIGH,
                                "Task quá hạn: "
                                        + task.getTitle(),
                                task.getId(),
                                task.getTitle(),
                                task.getAssigneeUserId(),
                                null,
                                "Cập nhật deadline hoặc xử lý task quá hạn ngay"
                        )
                );
            }

            if (task.getStatus() == TaskStatus.BLOCKED) {
                risks.add(
                        new SprintRiskResponse(
                                SprintRiskType.BLOCKED_TASK,
                                SprintRiskSeverity.HIGH,
                                "Task đang bị chặn: "
                                        + task.getTitle(),
                                task.getId(),
                                task.getTitle(),
                                task.getAssigneeUserId(),
                                null,
                                "Xử lý nguyên nhân blocker trước khi tiếp tục Sprint"
                        )
                );
            }

            if (task.getAssigneeUserId() == null
                    && !TERMINAL_STATUSES.contains(
                    task.getStatus()
            )) {

                risks.add(
                        new SprintRiskResponse(
                                SprintRiskType.NO_ASSIGNEE,
                                SprintRiskSeverity.MEDIUM,
                                "Task chưa phân công: "
                                        + task.getTitle(),
                                task.getId(),
                                task.getTitle(),
                                null,
                                null,
                                "Phân công người phụ trách để tránh trôi việc"
                        )
                );
            }

            if (task.getEstimatedMinutes() == null
                    && !TERMINAL_STATUSES.contains(
                    task.getStatus()
            )) {

                risks.add(
                        new SprintRiskResponse(
                                SprintRiskType.NO_ESTIMATE,
                                SprintRiskSeverity.LOW,
                                "Task chưa có estimate: "
                                        + task.getTitle(),
                                task.getId(),
                                task.getTitle(),
                                task.getAssigneeUserId(),
                                null,
                                "Bổ sung estimated minutes để theo dõi capacity chính xác hơn"
                        )
                );
            }
        }

        enrichRiskUsernames(risks);

        return risks.stream()
                .sorted(
                        Comparator.comparing(
                                this::severityRank
                        ).reversed()
                )
                .toList();
    }

    private void enrichRiskUsernames(
            List<SprintRiskResponse> risks
    ) {
        List<UUID> userIds =
                risks.stream()
                        .map(SprintRiskResponse::userId)
                        .filter(userId -> userId != null)
                        .distinct()
                        .toList();

        if (userIds.isEmpty()) {
            return;
        }

        Map<UUID, User> usersById =
                userRepository
                        .findAllById(userIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        User::getId,
                                        Function.identity()
                                )
                        );

        List<SprintRiskResponse> enriched =
                new ArrayList<>();

        for (SprintRiskResponse risk : risks) {
            User user =
                    risk.userId() == null
                            ? null
                            : usersById.get(
                            risk.userId()
                    );

            enriched.add(
                    new SprintRiskResponse(
                            risk.type(),
                            risk.severity(),
                            risk.message(),
                            risk.taskId(),
                            risk.taskTitle(),
                            risk.userId(),
                            user != null
                                    ? user.getUsername()
                                    : risk.username(),
                            risk.suggestedAction()
                    )
            );
        }

        risks.clear();
        risks.addAll(enriched);
    }

    // ===================== LOAD DATA =====================

    private Sprint getSprintWithAccess(
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

    private SprintCapacityResponse buildCapacityWithoutAccessCheck(
            UUID projectId,
            Sprint sprint
    ) {
        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        Map<UUID, User> usersById =
                loadUsersByMembers(members);

        Map<UUID, Long> estimatedByAssignee =
                loadEstimatedByAssignee(
                        projectId,
                        sprint.getId()
                );

        Map<UUID, Long> spentByUser =
                loadSpentByUser(
                        projectId,
                        sprint.getId()
                );

        Map<UUID, EnumMap<TaskStatus, Long>> statusCountByUser =
                loadStatusCountByUser(
                        projectId,
                        sprint.getId()
                );

        long sprintDays =
                calculateSprintDays(sprint);

        long memberCapacity =
                sprintDays * DAILY_CAPACITY_MINUTES;

        List<SprintCapacityMemberResponse> memberResponses =
                new ArrayList<>();

        long totalCapacityMinutes = 0L;
        long totalEstimatedMinutes = 0L;
        long totalSpentMinutes = 0L;

        for (ProjectMember member : members) {
            UUID userId =
                    member.getUserId();

            User user =
                    usersById.get(userId);

            long assignedEstimatedMinutes =
                    estimatedByAssignee.getOrDefault(
                            userId,
                            0L
                    );

            long spentMinutes =
                    spentByUser.getOrDefault(
                            userId,
                            0L
                    );

            EnumMap<TaskStatus, Long> counts =
                    statusCountByUser.getOrDefault(
                            userId,
                            emptyStatusCounts()
                    );

            long remainingCapacityMinutes =
                    Math.max(
                            memberCapacity
                                    - assignedEstimatedMinutes,
                            0L
                    );

            long overCapacityMinutes =
                    Math.max(
                            assignedEstimatedMinutes
                                    - memberCapacity,
                            0L
                    );

            long totalTasks =
                    counts.values()
                            .stream()
                            .mapToLong(Long::longValue)
                            .sum();

            totalCapacityMinutes += memberCapacity;
            totalEstimatedMinutes += assignedEstimatedMinutes;
            totalSpentMinutes += spentMinutes;

            memberResponses.add(
                    new SprintCapacityMemberResponse(
                            userId,
                            user != null
                                    ? user.getUsername()
                                    : null,
                            user != null
                                    ? user.getEmail()
                                    : null,
                            user != null
                                    ? user.getRole()
                                    : null,
                            member.getRole(),
                            memberCapacity,
                            assignedEstimatedMinutes,
                            spentMinutes,
                            remainingCapacityMinutes,
                            percentage(
                                    assignedEstimatedMinutes,
                                    memberCapacity
                            ),
                            overCapacityMinutes > 0,
                            overCapacityMinutes,
                            totalTasks,
                            counts.get(TaskStatus.TODO),
                            counts.get(TaskStatus.IN_PROGRESS),
                            counts.get(TaskStatus.IN_REVIEW),
                            counts.get(TaskStatus.DONE),
                            counts.get(TaskStatus.BLOCKED),
                            counts.get(TaskStatus.CANCELLED)
                    )
            );
        }

        long remainingCapacityMinutes =
                Math.max(
                        totalCapacityMinutes
                                - totalEstimatedMinutes,
                        0L
                );

        long overCapacityMinutes =
                Math.max(
                        totalEstimatedMinutes
                                - totalCapacityMinutes,
                        0L
                );

        return new SprintCapacityResponse(
                projectId,
                sprint.getId(),
                sprint.getName(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprintDays,
                members.size(),
                totalCapacityMinutes,
                totalEstimatedMinutes,
                totalSpentMinutes,
                remainingCapacityMinutes,
                percentage(
                        totalEstimatedMinutes,
                        totalCapacityMinutes
                ),
                overCapacityMinutes > 0,
                overCapacityMinutes,
                memberResponses
        );
    }

    private Map<UUID, User> loadUsersByMembers(
            Collection<ProjectMember> members
    ) {
        List<UUID> userIds =
                members.stream()
                        .map(ProjectMember::getUserId)
                        .distinct()
                        .toList();

        if (userIds.isEmpty()) {
            return new LinkedHashMap<>();
        }

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

    private Map<UUID, Long> loadEstimatedByAssignee(
            UUID projectId,
            UUID sprintId
    ) {
        Map<UUID, Long> result =
                new LinkedHashMap<>();

        for (Object[] row : taskRepository
                .sumEstimatedMinutesGroupedByAssigneeInSprint(
                        projectId,
                        sprintId
                )) {

            if (row != null
                    && row.length >= 2
                    && row[0] != null) {

                result.put(
                        (UUID) row[0],
                        toLong(row[1])
                );
            }
        }

        return result;
    }

    private Map<UUID, Long> loadSpentByUser(
            UUID projectId,
            UUID sprintId
    ) {
        Map<UUID, Long> result =
                new LinkedHashMap<>();

        for (Object[] row : taskTimeLogRepository
                .sumMinutesGroupedByUserInSprint(
                        projectId,
                        sprintId
                )) {

            if (row != null
                    && row.length >= 2
                    && row[0] != null) {

                result.put(
                        (UUID) row[0],
                        toLong(row[1])
                );
            }
        }

        return result;
    }

    private Map<UUID, EnumMap<TaskStatus, Long>>
    loadStatusCountByUser(
            UUID projectId,
            UUID sprintId
    ) {
        Map<UUID, EnumMap<TaskStatus, Long>> result =
                new LinkedHashMap<>();

        for (Object[] row : taskRepository
                .countTasksGroupedByAssigneeAndStatusInSprint(
                        projectId,
                        sprintId
                )) {

            if (row == null
                    || row.length < 3
                    || row[0] == null
                    || row[1] == null) {

                continue;
            }

            UUID userId =
                    (UUID) row[0];

            TaskStatus status =
                    (TaskStatus) row[1];

            EnumMap<TaskStatus, Long> counts =
                    result.computeIfAbsent(
                            userId,
                            ignored -> emptyStatusCounts()
                    );

            counts.put(
                    status,
                    toLong(row[2])
            );
        }

        return result;
    }

    // ===================== HEALTH HELPERS =====================

    private SprintHealthStatus resolveHealthStatus(
            List<SprintRiskResponse> risks,
            double progressRate,
            double expectedProgressRate
    ) {
        boolean hasCritical =
                risks.stream()
                        .anyMatch(risk ->
                                risk.severity()
                                        == SprintRiskSeverity.CRITICAL
                        );

        if (hasCritical) {
            return SprintHealthStatus.CRITICAL;
        }

        if (progressRate + 20.0 < expectedProgressRate) {
            return SprintHealthStatus.CRITICAL;
        }

        boolean hasHigh =
                risks.stream()
                        .anyMatch(risk ->
                                risk.severity()
                                        == SprintRiskSeverity.HIGH
                        );

        if (hasHigh
                || progressRate + 10.0 < expectedProgressRate) {

            return SprintHealthStatus.WARNING;
        }

        return SprintHealthStatus.GOOD;
    }

    private int severityRank(
            SprintRiskResponse risk
    ) {
        return switch (risk.severity()) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    // ===================== GENERAL HELPERS =====================

    private long calculateSprintDays(
            Sprint sprint
    ) {
        if (sprint.getStartDate() == null
                || sprint.getEndDate() == null) {
            return 0L;
        }

        if (sprint.getEndDate()
                .isBefore(
                        sprint.getStartDate()
                )) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(
                sprint.getStartDate(),
                sprint.getEndDate()
        ) + 1L;
    }

    private long calculateElapsedDays(
            Sprint sprint,
            LocalDate today
    ) {
        if (sprint.getStartDate() == null
                || sprint.getEndDate() == null) {
            return 0L;
        }

        if (today.isBefore(
                sprint.getStartDate()
        )) {
            return 0L;
        }

        LocalDate effectiveToday =
                today.isAfter(sprint.getEndDate())
                        ? sprint.getEndDate()
                        : today;

        return ChronoUnit.DAYS.between(
                sprint.getStartDate(),
                effectiveToday
        ) + 1L;
    }

    private long calculateRemainingDays(
            Sprint sprint,
            LocalDate today
    ) {
        if (sprint.getEndDate() == null) {
            return 0L;
        }

        if (today.isAfter(sprint.getEndDate())) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(
                today,
                sprint.getEndDate()
        );
    }

    private boolean isOverdue(
            Task task,
            LocalDate today
    ) {
        return task.getDueDate() != null
                && task.getDueDate().isBefore(today)
                && !TERMINAL_STATUSES.contains(
                task.getStatus()
        );
    }

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

    private EnumMap<TaskStatus, Long> emptyStatusCounts() {
        EnumMap<TaskStatus, Long> counts =
                new EnumMap<>(TaskStatus.class);

        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, 0L);
        }

        return counts;
    }

    private long toLong(
            Object value
    ) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return 0L;
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
