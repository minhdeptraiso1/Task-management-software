package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.timesheet.TimesheetSearchRequest;
import com.project.taskmanagement.dto.response.timesheet.TimesheetDailySummaryResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetEntryResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetPageResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetSummaryResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetUserSummaryResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetDailySummaryView;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetEntryView;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetUserSummaryView;
import com.project.taskmanagement.service.TimesheetService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
public class TimesheetServiceImpl
        implements TimesheetService {

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    static final int DEFAULT_RANGE_DAYS = 7;

    TaskTimeLogRepository taskTimeLogRepository;
    TaskRepository taskRepository;
    ProjectMemberRepository projectMemberRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    @Override
    @Transactional(readOnly = true)
    public TimesheetPageResponse getMyTimesheet(
            TimesheetSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        DateRange dateRange =
                resolveDateRange(request);

        UUID taskId =
                request != null
                        ? request.taskId()
                        : null;

        validateTaskFilterForUser(
                currentUser.getId(),
                taskId
        );

        Page<TimesheetEntryResponse> page =
                taskTimeLogRepository
                        .searchTimesheetEntries(
                                null,
                                currentUser.getId(),
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                pageable
                        )
                        .map(view ->
                                toEntryResponse(
                                        view,
                                        currentUser.getId()
                                )
                        );

        return TimesheetPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetSummaryResponse getMyTimesheetSummary(
            TimesheetSearchRequest request
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        DateRange dateRange =
                resolveDateRange(request);

        UUID taskId =
                request != null
                        ? request.taskId()
                        : null;

        validateTaskFilterForUser(
                currentUser.getId(),
                taskId
        );

        return buildSummary(
                null,
                currentUser.getId(),
                taskId,
                dateRange
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetPageResponse getProjectTimesheet(
            UUID projectId,
            TimesheetSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                requireProjectViewAccess(projectId);

        DateRange dateRange =
                resolveDateRange(request);

        UUID userId =
                request != null
                        ? request.userId()
                        : null;

        UUID taskId =
                request != null
                        ? request.taskId()
                        : null;

        validateProjectFilters(
                projectId,
                userId,
                taskId
        );

        Page<TimesheetEntryResponse> page =
                taskTimeLogRepository
                        .searchTimesheetEntries(
                                projectId,
                                userId,
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate(),
                                pageable
                        )
                        .map(view ->
                                toEntryResponse(
                                        view,
                                        currentUser.getId()
                                )
                        );

        return TimesheetPageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetSummaryResponse getProjectTimesheetSummary(
            UUID projectId,
            TimesheetSearchRequest request
    ) {
        requireProjectViewAccess(projectId);

        DateRange dateRange =
                resolveDateRange(request);

        UUID userId =
                request != null
                        ? request.userId()
                        : null;

        UUID taskId =
                request != null
                        ? request.taskId()
                        : null;

        validateProjectFilters(
                projectId,
                userId,
                taskId
        );

        return buildSummary(
                projectId,
                userId,
                taskId,
                dateRange
        );
    }

    private TimesheetSummaryResponse buildSummary(
            UUID projectId,
            UUID userId,
            UUID taskId,
            DateRange dateRange
    ) {
        return new TimesheetSummaryResponse(
                projectId,
                userId,
                dateRange.fromDate(),
                dateRange.toDate(),
                safeLong(
                        taskTimeLogRepository
                                .sumTimesheetMinutes(
                                        projectId,
                                        userId,
                                        taskId,
                                        dateRange.fromDate(),
                                        dateRange.toDate()
                                )
                ),
                taskTimeLogRepository
                        .countTimesheetLogs(
                                projectId,
                                userId,
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate()
                        ),
                taskTimeLogRepository
                        .countTimesheetTasks(
                                projectId,
                                userId,
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate()
                        ),
                taskTimeLogRepository
                        .countTimesheetUsers(
                                projectId,
                                userId,
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate()
                        ),
                buildDailySummary(
                        projectId,
                        userId,
                        taskId,
                        dateRange
                ),
                buildUserSummary(
                        projectId,
                        userId,
                        taskId,
                        dateRange
                )
        );
    }

    private List<TimesheetDailySummaryResponse> buildDailySummary(
            UUID projectId,
            UUID userId,
            UUID taskId,
            DateRange dateRange
    ) {
        Map<LocalDate, TimesheetDailySummaryView> rowsByDate =
                taskTimeLogRepository
                        .summarizeTimesheetByDate(
                                projectId,
                                userId,
                                taskId,
                                dateRange.fromDate(),
                                dateRange.toDate()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                TimesheetDailySummaryView::getWorkDate,
                                Function.identity(),
                                (left, right) -> left,
                                LinkedHashMap::new
                        ));

        java.util.ArrayList<TimesheetDailySummaryResponse> result =
                new java.util.ArrayList<>();

        LocalDate cursor =
                dateRange.fromDate();

        while (!cursor.isAfter(dateRange.toDate())) {
            TimesheetDailySummaryView row =
                    rowsByDate.get(cursor);

            result.add(
                    new TimesheetDailySummaryResponse(
                            cursor,
                            row == null
                                    ? 0L
                                    : safeLong(row.getTotalMinutes()),
                            row == null
                                    ? 0L
                                    : safeLong(row.getLogCount())
                    )
            );

            cursor =
                    cursor.plusDays(1);
        }

        return result;
    }

    private List<TimesheetUserSummaryResponse> buildUserSummary(
            UUID projectId,
            UUID userId,
            UUID taskId,
            DateRange dateRange
    ) {
        return taskTimeLogRepository
                .summarizeTimesheetByUser(
                        projectId,
                        userId,
                        taskId,
                        dateRange.fromDate(),
                        dateRange.toDate()
                )
                .stream()
                .map(this::toUserSummary)
                .toList();
    }

    private TimesheetEntryResponse toEntryResponse(
            TimesheetEntryView view,
            UUID currentUserId
    ) {
        boolean owner =
                view.getUserId() != null
                        && view.getUserId()
                        .equals(currentUserId);

        return new TimesheetEntryResponse(
                view.getId(),
                view.getProjectId(),
                view.getProjectCode(),
                view.getProjectName(),
                view.getTaskId(),
                view.getTaskTitle(),
                view.getUserId(),
                view.getUsername(),
                view.getEmail(),
                view.getWorkDate(),
                view.getMinutes(),
                view.getDescription(),
                owner,
                owner,
                view.getCreatedAt(),
                view.getUpdatedAt()
        );
    }

    private TimesheetUserSummaryResponse toUserSummary(
            TimesheetUserSummaryView view
    ) {
        return new TimesheetUserSummaryResponse(
                view.getUserId(),
                view.getUsername(),
                view.getEmail(),
                safeLong(view.getTotalMinutes()),
                safeLong(view.getLogCount()),
                safeLong(view.getTaskCount())
        );
    }

    private User requireProjectViewAccess(
            UUID projectId
    ) {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        Project project =
                projectAccessService.getProjectOrThrow(projectId);

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        return currentUser;
    }

    private void validateProjectFilters(
            UUID projectId,
            UUID userId,
            UUID taskId
    ) {
        if (userId != null
                && projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                )
                .isEmpty()) {

            throw new BusinessException(
                    ErrorCode.REPORT_USER_NOT_PROJECT_MEMBER
            );
        }

        if (taskId != null
                && taskRepository
                .findByIdAndProjectId(
                        taskId,
                        projectId
                )
                .isEmpty()) {

            throw new BusinessException(
                    ErrorCode.REPORT_TASK_NOT_IN_PROJECT
            );
        }
    }

    private void validateTaskFilterForUser(
            UUID currentUserId,
            UUID taskId
    ) {
        if (taskId == null) {
            return;
        }

        Task task =
                taskRepository
                        .findById(taskId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TASK_NOT_FOUND
                                )
                        );

        if (projectMemberRepository
                .findByProjectIdAndUserId(
                        task.getProjectId(),
                        currentUserId
                )
                .isEmpty()) {

            throw new BusinessException(
                    ErrorCode.PROJECT_ACCESS_DENIED
            );
        }
    }

    private DateRange resolveDateRange(
            TimesheetSearchRequest request
    ) {
        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        LocalDate fromDate =
                request != null
                        && request.fromDate() != null
                        ? request.fromDate()
                        : today.minusDays(DEFAULT_RANGE_DAYS - 1L);

        LocalDate toDate =
                request != null
                        && request.toDate() != null
                        ? request.toDate()
                        : today;

        if (fromDate.isAfter(toDate)) {
            throw new BusinessException(
                    ErrorCode.REPORT_DATE_RANGE_INVALID
            );
        }

        return new DateRange(
                fromDate,
                toDate
        );
    }

    private long safeLong(
            Number value
    ) {
        return value == null
                ? 0L
                : value.longValue();
    }

    private record DateRange(
            LocalDate fromDate,
            LocalDate toDate
    ) {
    }
}
