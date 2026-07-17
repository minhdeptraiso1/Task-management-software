package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.bugreport.BugReportRequest;
import com.project.taskmanagement.dto.response.bugreport.BugAssigneeSummaryResponse;
import com.project.taskmanagement.dto.response.bugreport.BugCountByPriorityResponse;
import com.project.taskmanagement.dto.response.bugreport.BugCountBySeverityResponse;
import com.project.taskmanagement.dto.response.bugreport.BugCountByStatusResponse;
import com.project.taskmanagement.dto.response.bugreport.BugDashboardResponse;
import com.project.taskmanagement.dto.response.bugreport.BugReportResponse;
import com.project.taskmanagement.dto.response.bugreport.QaMetricsResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.projection.bugreport.BugAssigneeSummaryView;
import com.project.taskmanagement.repository.projection.bugreport.BugPriorityCountView;
import com.project.taskmanagement.repository.projection.bugreport.BugSeverityCountView;
import com.project.taskmanagement.repository.projection.bugreport.BugStatusCountView;
import com.project.taskmanagement.service.BugReportService;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugReportServiceImpl implements BugReportService {

    BugRepository bugRepository;
    SprintRepository sprintRepository;
    ProjectMemberRepository projectMemberRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    static final int MAX_RANGE_DAYS = 366;
    static final List<BugStatus> DONE_STATUSES =
            List.of(BugStatus.RESOLVED, BugStatus.VERIFIED, BugStatus.CLOSED, BugStatus.CANCELLED);

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_DASHBOARD, key = "#projectId")
    public BugDashboardResponse getProjectBugDashboard(UUID projectId) {
        Project project = requireProjectViewAccess(projectId);
        DateRange range = defaultAllTimeRange();
        BugReportCore core = buildCore(projectId, null, null, range);

        return new BugDashboardResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                core.totalBugs(),
                core.openBugs(),
                core.inProgressBugs(),
                core.resolvedBugs(),
                core.closedBugs(),
                core.cancelledBugs(),
                core.overdueBugs(),
                core.criticalBugs(),
                core.reopenedBugs(),
                core.resolveRate(),
                core.byStatus(),
                core.bySeverity(),
                core.byPriority(),
                core.byAssignee()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_REPORT, key = "{#projectId,#request}")
    public BugReportResponse getProjectBugReport(UUID projectId, BugReportRequest request) {
        requireProjectViewAccess(projectId);
        DateRange range = resolveDateRange(request);
        UUID sprintId = request == null ? null : request.sprintId();
        validateFilters(projectId, sprintId, request == null ? null : request.assigneeUserId());
        return toReportResponse(projectId, sprintId, request, range);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_REPORT, key = "{#projectId,#sprintId,#request}")
    public BugReportResponse getSprintBugReport(UUID projectId, UUID sprintId, BugReportRequest request) {
        requireProjectViewAccess(projectId);
        validateFilters(projectId, sprintId, request == null ? null : request.assigneeUserId());
        DateRange range = resolveDateRange(request);
        return toReportResponse(projectId, sprintId, request, range);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BUG_QA_METRICS, key = "{#projectId,#request}")
    public QaMetricsResponse getQaMetrics(UUID projectId, BugReportRequest request) {
        requireProjectViewAccess(projectId);
        DateRange range = resolveDateRange(request);
        UUID sprintId = request == null ? null : request.sprintId();
        validateFilters(projectId, sprintId, request == null ? null : request.assigneeUserId());
        BugReportCore core = buildCore(projectId, sprintId, request, range);

        return new QaMetricsResponse(
                projectId,
                range.fromDate(),
                range.toDate(),
                core.totalBugs(),
                core.resolvedBugs(),
                core.closedBugs(),
                core.reopenedBugs(),
                core.criticalBugs(),
                core.overdueBugs(),
                core.resolveRate(),
                percent(core.reopenedBugs(), core.totalBugs()),
                percent(core.overdueBugs(), core.totalBugs()),
                percent(core.criticalBugs(), core.totalBugs())
        );
    }

    private BugReportResponse toReportResponse(UUID projectId, UUID sprintId, BugReportRequest request, DateRange range) {
        BugReportCore core = buildCore(projectId, sprintId, request, range);
        return new BugReportResponse(
                projectId,
                sprintId,
                range.fromDate(),
                range.toDate(),
                core.totalBugs(),
                core.openBugs(),
                core.resolvedBugs(),
                core.closedBugs(),
                core.overdueBugs(),
                core.criticalBugs(),
                core.reopenedBugs(),
                core.resolveRate(),
                percent(core.overdueBugs(), core.totalBugs()),
                core.byStatus(),
                core.bySeverity(),
                core.byPriority(),
                core.byAssignee()
        );
    }

    private BugReportCore buildCore(UUID projectId, UUID sprintId, BugReportRequest request, DateRange range) {
        Instant fromDateStart = range.fromDate().atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant toDateExclusive = range.toDate().plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        UUID assigneeUserId = request == null ? null : request.assigneeUserId();
        BugStatus status = request == null ? null : request.status();
        BugSeverity severity = request == null ? null : request.severity();
        TaskPriority priority = request == null ? null : request.priority();
        boolean overdueOnly = request != null && Boolean.TRUE.equals(request.overdueOnly());

        long totalBugs = bugRepository.countForReport(projectId, sprintId, assigneeUserId, status, severity, priority,
                overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive);
        long openBugs = countByStatus(projectId, sprintId, assigneeUserId, status, BugStatus.OPEN, severity, priority,
                overdueOnly, today, fromDateStart, toDateExclusive);
        long inProgressBugs = countByStatus(projectId, sprintId, assigneeUserId, status, BugStatus.IN_PROGRESS, severity, priority,
                overdueOnly, today, fromDateStart, toDateExclusive);
        long resolvedBugs = countByStatus(projectId, sprintId, assigneeUserId, status, BugStatus.RESOLVED, severity, priority,
                overdueOnly, today, fromDateStart, toDateExclusive);
        long closedBugs = countByStatus(projectId, sprintId, assigneeUserId, status, BugStatus.CLOSED, severity, priority,
                overdueOnly, today, fromDateStart, toDateExclusive);
        long cancelledBugs = countByStatus(projectId, sprintId, assigneeUserId, status, BugStatus.CANCELLED, severity, priority,
                overdueOnly, today, fromDateStart, toDateExclusive);
        long overdueBugs = bugRepository.countOverdueForReport(projectId, sprintId, assigneeUserId, status, severity,
                priority, today, DONE_STATUSES, fromDateStart, toDateExclusive);
        long criticalBugs = severity != null && severity != BugSeverity.CRITICAL
                ? 0L
                : bugRepository.countForReport(projectId, sprintId, assigneeUserId, status, BugSeverity.CRITICAL,
                priority, overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive);
        long reopenedBugs = safeLong(bugRepository.sumReopenedCountForReport(projectId, sprintId, assigneeUserId, status,
                severity, priority, overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive));

        return new BugReportCore(
                totalBugs,
                openBugs,
                inProgressBugs,
                resolvedBugs,
                closedBugs,
                cancelledBugs,
                overdueBugs,
                criticalBugs,
                reopenedBugs,
                percent(resolvedBugs + closedBugs, totalBugs),
                fillStatusCounts(bugRepository.countGroupedByStatus(projectId, sprintId, assigneeUserId, status, severity,
                        priority, overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive)),
                fillSeverityCounts(bugRepository.countGroupedBySeverity(projectId, sprintId, assigneeUserId, status, severity,
                        priority, overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive)),
                fillPriorityCounts(bugRepository.countGroupedByPriority(projectId, sprintId, assigneeUserId, status, severity,
                        priority, overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive)),
                bugRepository.summarizeByAssignee(projectId, sprintId, assigneeUserId, status, severity, priority,
                                overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive)
                        .stream()
                        .map(this::toAssigneeSummary)
                        .toList()
        );
    }

    private long countByStatus(UUID projectId, UUID sprintId, UUID assigneeUserId, BugStatus requestedStatus, BugStatus status,
                               BugSeverity severity, TaskPriority priority, boolean overdueOnly, LocalDate today,
                               Instant fromDateStart, Instant toDateExclusive) {
        if (requestedStatus != null && requestedStatus != status) {
            return 0L;
        }
        return bugRepository.countForReportByStatus(projectId, sprintId, assigneeUserId, status, severity, priority,
                overdueOnly, today, DONE_STATUSES, fromDateStart, toDateExclusive);
    }

    private BugAssigneeSummaryResponse toAssigneeSummary(BugAssigneeSummaryView view) {
        return new BugAssigneeSummaryResponse(
                view.getAssigneeUserId(),
                view.getUsername(),
                view.getEmail(),
                safeLong(view.getTotalBugs()),
                safeLong(view.getOpenBugs()),
                safeLong(view.getResolvedBugs()),
                safeLong(view.getClosedBugs()),
                safeLong(view.getOverdueBugs()),
                safeLong(view.getCriticalBugs()),
                safeLong(view.getReopenedBugs())
        );
    }

    private List<BugCountByStatusResponse> fillStatusCounts(List<BugStatusCountView> rows) {
        EnumMap<BugStatus, Long> map = new EnumMap<>(BugStatus.class);
        for (BugStatus status : BugStatus.values()) {
            map.put(status, 0L);
        }
        for (BugStatusCountView row : rows) {
            map.put(row.getStatus(), safeLong(row.getTotal()));
        }
        List<BugCountByStatusResponse> result = new ArrayList<>();
        for (BugStatus status : BugStatus.values()) {
            result.add(new BugCountByStatusResponse(status, map.get(status)));
        }
        return result;
    }

    private List<BugCountBySeverityResponse> fillSeverityCounts(List<BugSeverityCountView> rows) {
        EnumMap<BugSeverity, Long> map = new EnumMap<>(BugSeverity.class);
        for (BugSeverity severity : BugSeverity.values()) {
            map.put(severity, 0L);
        }
        for (BugSeverityCountView row : rows) {
            map.put(row.getSeverity(), safeLong(row.getTotal()));
        }
        List<BugCountBySeverityResponse> result = new ArrayList<>();
        for (BugSeverity severity : BugSeverity.values()) {
            result.add(new BugCountBySeverityResponse(severity, map.get(severity)));
        }
        return result;
    }

    private List<BugCountByPriorityResponse> fillPriorityCounts(List<BugPriorityCountView> rows) {
        EnumMap<TaskPriority, Long> map = new EnumMap<>(TaskPriority.class);
        for (TaskPriority priority : TaskPriority.values()) {
            map.put(priority, 0L);
        }
        for (BugPriorityCountView row : rows) {
            map.put(row.getPriority(), safeLong(row.getTotal()));
        }
        List<BugCountByPriorityResponse> result = new ArrayList<>();
        for (TaskPriority priority : TaskPriority.values()) {
            result.add(new BugCountByPriorityResponse(priority, map.get(priority)));
        }
        return result;
    }

    private Project requireProjectViewAccess(UUID projectId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        return project;
    }

    private void validateFilters(UUID projectId, UUID sprintId, UUID assigneeUserId) {
        if (sprintId != null && sprintRepository.findByIdAndProjectId(sprintId, projectId).isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_SPRINT_NOT_IN_PROJECT);
        }
        if (assigneeUserId != null && projectMemberRepository.findByProjectIdAndUserId(projectId, assigneeUserId).isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_ASSIGNEE_NOT_PROJECT_MEMBER);
        }
    }

    private DateRange resolveDateRange(BugReportRequest request) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate fromDate = request != null && request.fromDate() != null
                ? request.fromDate()
                : today.minusDays(29);
        LocalDate toDate = request != null && request.toDate() != null
                ? request.toDate()
                : today;

        if (fromDate.isAfter(toDate)) {
            throw new BusinessException(ErrorCode.BUG_REPORT_DATE_RANGE_INVALID);
        }
        if (fromDate.plusDays(MAX_RANGE_DAYS).isBefore(toDate)) {
            throw new BusinessException(ErrorCode.BUG_REPORT_DATE_RANGE_TOO_LARGE);
        }
        return new DateRange(fromDate, toDate);
    }

    private DateRange defaultAllTimeRange() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        return new DateRange(today.minusYears(10), today);
    }

    private double percent(long value, long total) {
        if (total <= 0) {
            return 0D;
        }
        return Math.round(value * 10000D / total) / 100D;
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private record DateRange(LocalDate fromDate, LocalDate toDate) {
    }

    private record BugReportCore(
            long totalBugs,
            long openBugs,
            long inProgressBugs,
            long resolvedBugs,
            long closedBugs,
            long cancelledBugs,
            long overdueBugs,
            long criticalBugs,
            long reopenedBugs,
            double resolveRate,
            List<BugCountByStatusResponse> byStatus,
            List<BugCountBySeverityResponse> bySeverity,
            List<BugCountByPriorityResponse> byPriority,
            List<BugAssigneeSummaryResponse> byAssignee
    ) {
    }
}
