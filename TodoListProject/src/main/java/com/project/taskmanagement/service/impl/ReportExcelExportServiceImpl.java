package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.dto.response.analytics.BurnupChartResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;
import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.projection.bug.BugExportRowView;
import com.project.taskmanagement.repository.projection.report.ReportMemberPerformanceExcelView;
import com.project.taskmanagement.repository.projection.report.ReportTimeLogExcelView;
import com.project.taskmanagement.service.AnalyticsService;
import com.project.taskmanagement.service.ReportExcelExportService;
import com.project.taskmanagement.service.TaskStatisticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.GeneratedReportFile;
import com.project.taskmanagement.service.report.ReportContentTypes;
import com.project.taskmanagement.service.report.ReportFileNameBuilder;
import com.project.taskmanagement.service.report.excel.ExcelReportHelper;
import com.project.taskmanagement.service.report.excel.ReportSheetNames;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportExcelExportServiceImpl implements ReportExcelExportService {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    BugRepository bugRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskStatisticsService taskStatisticsService;
    ProjectAccessService projectAccessService;
    CurrentUserService currentUserService;
    ExcelReportHelper excelReportHelper;
    AnalyticsService analyticsService;
    ReportFileNameBuilder reportFileNameBuilder;

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportSprintReport(UUID projectId, UUID sprintId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);

        Sprint sprint = getSprintOrThrow(projectId, sprintId);
        SprintTaskStatisticsResponse statistics = taskStatisticsService.getSprintStatistics(projectId, sprintId);
        List<BacklogItem> backlogItems = backlogItemRepository
                .findAllByProjectIdAndSprintIdOrderByPositionAsc(projectId, sprintId);
        List<Task> tasks = taskRepository
                .findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(projectId, sprintId);

        try (XSSFWorkbook workbook = excelReportHelper.createWorkbook()) {
            createSprintSummarySheet(workbook, project, sprint, statistics);
            createTasksSheet(workbook, tasks);
            createBacklogItemsSheet(workbook, backlogItems);
            createTimeLogsSheet(workbook, projectId, sprintId, null, null, null);
            createMemberPerformanceSheet(workbook, projectId, sprintId, null, null, null);
            createBurnupSheet(workbook, projectId, sprintId);
            createCumulativeFlowSheet(workbook, projectId, sprintId);

            return buildFile(workbook, reportFileNameBuilder.sprintExcel(project, sprint));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportProjectReport(UUID projectId, ProjectExcelReportRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        validateProjectReportRequest(projectId, request);

        List<Sprint> sprints = sprintRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId);
        List<BacklogItem> backlogItems = loadProjectBacklogItems(projectId, request);
        List<Task> tasks = loadProjectTasks(projectId, request);
        List<BugExportRowView> bugs = loadProjectBugs(projectId, request);

        try (XSSFWorkbook workbook = excelReportHelper.createWorkbook()) {
            createProjectSummarySheet(workbook, project, request, sprints.size(), backlogItems.size(), tasks.size(), bugs.size());
            createSprintsSheet(workbook, sprints);
            createBacklogItemsSheet(workbook, backlogItems);
            createTasksSheet(workbook, tasks);
            createTimeLogsSheet(workbook, projectId, sprintIdOf(request), userIdOf(request), fromDateOf(request), toDateOf(request));
            createMemberPerformanceSheet(workbook, projectId, sprintIdOf(request), userIdOf(request), fromDateOf(request), toDateOf(request));
            createBugSummarySheet(workbook, bugs);
            createBugListSheet(workbook, bugs);
            createVelocitySheet(workbook, projectId);
            if (sprintIdOf(request) != null) {
                createBurnupSheet(workbook, projectId, sprintIdOf(request));
                createCumulativeFlowSheet(workbook, projectId, sprintIdOf(request));
            }

            return buildFile(workbook, reportFileNameBuilder.projectExcel(project));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    private void createSprintSummarySheet(
            XSSFWorkbook workbook,
            Project project,
            Sprint sprint,
            SprintTaskStatisticsResponse statistics
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.SPRINT_SUMMARY);
        excelReportHelper.createHeaderRow(sheet, "Field", "Value");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Project Code", project.getCode());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Project Name", project.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Sprint Name", sprint.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Sprint Status", sprint.getStatus());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Start Date", sprint.getStartDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "End Date", sprint.getEndDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Total Tasks", statistics.totalTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Completed Tasks", statistics.completedTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Blocked Tasks", statistics.blockedTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Overdue Tasks", statistics.overdueTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Completion Rate", statistics.completionRate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Estimated Minutes", excelReportHelper.minutesToHourText(statistics.estimatedMinutes()));
        addKeyValueRow(sheet, rowIndex, "Spent Minutes", excelReportHelper.minutesToHourText(statistics.spentMinutes()));

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createProjectSummarySheet(
            XSSFWorkbook workbook,
            Project project,
            ProjectExcelReportRequest request,
            int sprintCount,
            int backlogItemCount,
            int taskCount,
            int bugCount
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.PROJECT_SUMMARY);
        excelReportHelper.createHeaderRow(sheet, "Field", "Value");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Project Code", project.getCode());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Project Name", project.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Status", project.getStatus());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Start Date", project.getStartDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "End Date", project.getEndDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Sprint Count", sprintCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Backlog Item Count", backlogItemCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Task Count", taskCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug Count", bugCount);

        if (request != null) {
            rowIndex = addKeyValueRow(sheet, rowIndex, "Filter From Date", request.fromDate());
            rowIndex = addKeyValueRow(sheet, rowIndex, "Filter To Date", request.toDate());
            rowIndex = addKeyValueRow(sheet, rowIndex, "Filter Sprint Id", request.sprintId());
            addKeyValueRow(sheet, rowIndex, "Filter User Id", request.userId());
        }

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createSprintsSheet(XSSFWorkbook workbook, List<Sprint> sprints) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.SPRINTS);
        int columnCount = 9;
        excelReportHelper.createHeaderRow(
                sheet,
                "Sprint Id",
                "Name",
                "Goal",
                "Status",
                "Start Date",
                "End Date",
                "Started At",
                "Completed At",
                "Created At"
        );

        int rowIndex = 1;
        for (Sprint sprint : sprints) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, sprint.getId());
            excelReportHelper.setCell(row, column++, sprint.getName());
            excelReportHelper.setCell(row, column++, sprint.getGoal());
            excelReportHelper.setCell(row, column++, sprint.getStatus());
            excelReportHelper.setCell(row, column++, sprint.getStartDate());
            excelReportHelper.setCell(row, column++, sprint.getEndDate());
            excelReportHelper.setCell(row, column++, sprint.getStartedAt());
            excelReportHelper.setCell(row, column++, sprint.getCompletedAt());
            excelReportHelper.setCell(row, column, sprint.getCreatedAt());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createBacklogItemsSheet(XSSFWorkbook workbook, List<BacklogItem> backlogItems) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BACKLOG_ITEMS);
        int columnCount = 9;
        excelReportHelper.createHeaderRow(
                sheet,
                "Backlog Item Id",
                "Sprint Id",
                "Title",
                "Type",
                "Status",
                "Priority",
                "Story Points",
                "Position",
                "Created At"
        );

        int rowIndex = 1;
        for (BacklogItem item : backlogItems) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, item.getId());
            excelReportHelper.setCell(row, column++, item.getSprintId());
            excelReportHelper.setCell(row, column++, item.getTitle());
            excelReportHelper.setCell(row, column++, item.getType());
            excelReportHelper.setCell(row, column++, item.getStatus());
            excelReportHelper.setCell(row, column++, item.getPriority());
            excelReportHelper.setCell(row, column++, item.getStoryPoints());
            excelReportHelper.setCell(row, column++, item.getPosition());
            excelReportHelper.setCell(row, column, item.getCreatedAt());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createTasksSheet(XSSFWorkbook workbook, List<Task> tasks) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.TASKS);
        int columnCount = 15;
        excelReportHelper.createHeaderRow(
                sheet,
                "Task Id",
                "Backlog Item Id",
                "Current Sprint Id",
                "Title",
                "Type",
                "Priority",
                "Status",
                "Assignee User Id",
                "Reporter User Id",
                "Estimated Minutes",
                "Start Date",
                "Due Date",
                "Completed At",
                "Position",
                "Created At"
        );

        int rowIndex = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, task.getId());
            excelReportHelper.setCell(row, column++, task.getBacklogItemId());
            excelReportHelper.setCell(row, column++, task.getCurrentSprintId());
            excelReportHelper.setCell(row, column++, task.getTitle());
            excelReportHelper.setCell(row, column++, task.getType());
            excelReportHelper.setCell(row, column++, task.getPriority());
            excelReportHelper.setCell(row, column++, task.getStatus());
            excelReportHelper.setCell(row, column++, task.getAssigneeUserId());
            excelReportHelper.setCell(row, column++, task.getReporterUserId());
            excelReportHelper.setCell(row, column++, task.getEstimatedMinutes());
            excelReportHelper.setCell(row, column++, task.getStartDate());
            excelReportHelper.setCell(row, column++, task.getDueDate());
            excelReportHelper.setCell(row, column++, task.getCompletedAt());
            excelReportHelper.setCell(row, column++, task.getPosition());
            excelReportHelper.setCell(row, column, task.getCreatedAt());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createTimeLogsSheet(
            XSSFWorkbook workbook,
            UUID projectId,
            UUID sprintId,
            UUID userId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.TIME_LOGS);
        int columnCount = 13;
        excelReportHelper.createHeaderRow(
                sheet,
                "Time Log Id",
                "Project Id",
                "Task Id",
                "Task Title",
                "Sprint Id",
                "Backlog Item Id",
                "User Id",
                "Username",
                "Email",
                "Work Date",
                "Minutes",
                "Hours",
                "Description"
        );

        List<ReportTimeLogExcelView> rows = taskTimeLogRepository.findTimeLogsForExcelReport(
                projectId,
                sprintId,
                userId,
                fromDate,
                toDate
        );

        int rowIndex = 1;
        for (ReportTimeLogExcelView item : rows) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, item.getTimeLogId());
            excelReportHelper.setCell(row, column++, item.getProjectId());
            excelReportHelper.setCell(row, column++, item.getTaskId());
            excelReportHelper.setCell(row, column++, item.getTaskTitle());
            excelReportHelper.setCell(row, column++, item.getSprintId());
            excelReportHelper.setCell(row, column++, item.getBacklogItemId());
            excelReportHelper.setCell(row, column++, item.getUserId());
            excelReportHelper.setCell(row, column++, item.getUsername());
            excelReportHelper.setCell(row, column++, item.getEmail());
            excelReportHelper.setCell(row, column++, item.getWorkDate());
            excelReportHelper.setCell(row, column++, item.getMinutes());
            excelReportHelper.setCell(row, column++, excelReportHelper.minutesToHourText(item.getMinutes()));
            excelReportHelper.setCell(row, column, item.getDescription());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createMemberPerformanceSheet(
            XSSFWorkbook workbook,
            UUID projectId,
            UUID sprintId,
            UUID userId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.MEMBER_PERFORMANCE);
        int columnCount = 12;
        excelReportHelper.createHeaderRow(
                sheet,
                "User Id",
                "Username",
                "Email",
                "Total Tasks",
                "Done Tasks",
                "Active Tasks",
                "Blocked Tasks",
                "Overdue Tasks",
                "Estimated Minutes",
                "Estimated Hours",
                "Spent Minutes",
                "Spent Hours"
        );

        List<ReportMemberPerformanceExcelView> rows = taskRepository.findMemberPerformanceForExcelReport(
                projectId,
                sprintId,
                userId,
                fromDate,
                toDate,
                LocalDate.now(BUSINESS_ZONE)
        );

        int rowIndex = 1;
        for (ReportMemberPerformanceExcelView item : rows) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, item.getUserId());
            excelReportHelper.setCell(row, column++, item.getUsername());
            excelReportHelper.setCell(row, column++, item.getEmail());
            excelReportHelper.setCell(row, column++, safeLong(item.getTotalTasks()));
            excelReportHelper.setCell(row, column++, safeLong(item.getDoneTasks()));
            excelReportHelper.setCell(row, column++, safeLong(item.getActiveTasks()));
            excelReportHelper.setCell(row, column++, safeLong(item.getBlockedTasks()));
            excelReportHelper.setCell(row, column++, safeLong(item.getOverdueTasks()));
            excelReportHelper.setCell(row, column++, safeLong(item.getEstimatedMinutes()));
            excelReportHelper.setCell(row, column++, excelReportHelper.minutesToHourText(item.getEstimatedMinutes()));
            excelReportHelper.setCell(row, column++, safeLong(item.getSpentMinutes()));
            excelReportHelper.setCell(row, column, excelReportHelper.minutesToHourText(item.getSpentMinutes()));
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createBugSummarySheet(XSSFWorkbook workbook, List<BugExportRowView> bugs) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BUG_SUMMARY);
        excelReportHelper.createHeaderRow(sheet, "Field", "Value");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Total Bugs", bugs.size());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Open Bugs", bugs.stream().filter(this::isOpenBug).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Resolved Bugs", bugs.stream().filter(item -> item.getStatus() == BugStatus.RESOLVED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Closed Bugs", bugs.stream().filter(item -> item.getStatus() == BugStatus.CLOSED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Verified Bugs", bugs.stream().filter(item -> item.getStatus() == BugStatus.VERIFIED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Critical Bugs", bugs.stream().filter(item -> item.getSeverity() == BugSeverity.CRITICAL).count());
        addKeyValueRow(sheet, rowIndex, "Reopened Count", bugs.stream().mapToLong(item -> safeLong(item.getReopenedCount())).sum());

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createBugListSheet(XSSFWorkbook workbook, List<BugExportRowView> bugs) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BUG_LIST);
        int columnCount = 17;
        excelReportHelper.createHeaderRow(
                sheet,
                "Bug Id",
                "Title",
                "Status",
                "Severity",
                "Priority",
                "Sprint",
                "Task",
                "Backlog Item",
                "Assignee",
                "Reporter",
                "Due Date",
                "Reopened Count",
                "Resolved At",
                "Closed At",
                "Created At",
                "Updated At",
                "Description"
        );

        int rowIndex = 1;
        for (BugExportRowView bug : bugs) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, bug.getId());
            excelReportHelper.setCell(row, column++, bug.getTitle());
            excelReportHelper.setCell(row, column++, bug.getStatus());
            excelReportHelper.setCell(row, column++, bug.getSeverity());
            excelReportHelper.setCell(row, column++, bug.getPriority());
            excelReportHelper.setCell(row, column++, bug.getSprintName());
            excelReportHelper.setCell(row, column++, bug.getTaskTitle());
            excelReportHelper.setCell(row, column++, bug.getBacklogItemTitle());
            excelReportHelper.setCell(row, column++, displayUser(bug.getAssigneeUsername(), bug.getAssigneeEmail()));
            excelReportHelper.setCell(row, column++, displayUser(bug.getReporterUsername(), bug.getReporterEmail()));
            excelReportHelper.setCell(row, column++, bug.getDueDate());
            excelReportHelper.setCell(row, column++, bug.getReopenedCount());
            excelReportHelper.setCell(row, column++, bug.getResolvedAt());
            excelReportHelper.setCell(row, column++, bug.getClosedAt());
            excelReportHelper.setCell(row, column++, bug.getCreatedAt());
            excelReportHelper.setCell(row, column++, bug.getUpdatedAt());
            excelReportHelper.setCell(row, column, bug.getDescription());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createVelocitySheet(XSSFWorkbook workbook, UUID projectId) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.VELOCITY);
        int columnCount = 10;
        excelReportHelper.createHeaderRow(
                sheet,
                "Sprint Id",
                "Sprint Name",
                "Sprint Status",
                "Start Date",
                "End Date",
                "Committed Items",
                "Completed Items",
                "Committed Story Points",
                "Completed Story Points",
                "Completion Rate"
        );

        VelocityChartResponse velocity = analyticsService.getVelocity(projectId);
        int rowIndex = 1;
        for (var item : velocity.points()) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, item.sprintId());
            excelReportHelper.setCell(row, column++, item.sprintName());
            excelReportHelper.setCell(row, column++, item.sprintStatus());
            excelReportHelper.setCell(row, column++, item.startDate());
            excelReportHelper.setCell(row, column++, item.endDate());
            excelReportHelper.setCell(row, column++, item.committedItems());
            excelReportHelper.setCell(row, column++, item.completedItems());
            excelReportHelper.setCell(row, column++, item.committedStoryPoints());
            excelReportHelper.setCell(row, column++, item.completedStoryPoints());
            excelReportHelper.setCell(row, column, item.completionRate());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createBurnupSheet(XSSFWorkbook workbook, UUID projectId, UUID sprintId) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BURNUP);
        int columnCount = 4;
        excelReportHelper.createHeaderRow(
                sheet,
                "Date",
                "Total Scope",
                "Completed Scope",
                "Completion Rate"
        );

        BurnupChartResponse burnup = analyticsService.getSprintBurnup(projectId, sprintId);
        int rowIndex = 1;
        for (var point : burnup.points()) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, point.date());
            excelReportHelper.setCell(row, column++, point.totalScope());
            excelReportHelper.setCell(row, column++, point.completedScope());
            excelReportHelper.setCell(row, column, point.completionRate());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private void createCumulativeFlowSheet(XSSFWorkbook workbook, UUID projectId, UUID sprintId) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.CUMULATIVE_FLOW);
        int columnCount = 7;
        excelReportHelper.createHeaderRow(
                sheet,
                "Date",
                "TODO",
                "IN_PROGRESS",
                "IN_REVIEW",
                "BLOCKED",
                "DONE",
                "CANCELLED"
        );

        CumulativeFlowResponse cumulativeFlow = analyticsService.getSprintCumulativeFlow(projectId, sprintId);
        int rowIndex = 1;
        for (var point : cumulativeFlow.points()) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, point.date());
            excelReportHelper.setCell(row, column++, point.todo());
            excelReportHelper.setCell(row, column++, point.inProgress());
            excelReportHelper.setCell(row, column++, point.inReview());
            excelReportHelper.setCell(row, column++, point.blocked());
            excelReportHelper.setCell(row, column++, point.done());
            excelReportHelper.setCell(row, column, point.cancelled());
        }

        excelReportHelper.applyAutoFilter(sheet, columnCount);
        excelReportHelper.autoSizeColumns(sheet, columnCount);
    }

    private int addKeyValueRow(Sheet sheet, int rowIndex, String key, Object value) {
        Row row = sheet.createRow(rowIndex);
        excelReportHelper.setCell(row, 0, key);
        excelReportHelper.setCell(row, 1, value);
        return rowIndex + 1;
    }

    private List<BacklogItem> loadProjectBacklogItems(UUID projectId, ProjectExcelReportRequest request) {
        if (request != null && request.sprintId() != null) {
            return backlogItemRepository.findAllByProjectIdAndSprintIdOrderByPositionAsc(projectId, request.sprintId());
        }
        return backlogItemRepository.findAllByProjectIdOrderByPositionAsc(projectId);
    }

    private List<Task> loadProjectTasks(UUID projectId, ProjectExcelReportRequest request) {
        List<Task> tasks = request != null && request.sprintId() != null
                ? taskRepository.findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(projectId, request.sprintId())
                : taskRepository.findAllByProjectId(projectId);

        if (request == null) {
            return tasks;
        }

        return tasks.stream()
                .filter(task -> matchesUserFilter(task, request.userId()))
                .filter(task -> matchesDateRange(task, request.fromDate(), request.toDate()))
                .toList();
    }

    private List<BugExportRowView> loadProjectBugs(UUID projectId, ProjectExcelReportRequest request) {
        LocalDate fromDate = fromDateOf(request) == null ? LocalDate.of(1970, 1, 1) : fromDateOf(request);
        LocalDate toDate = toDateOf(request) == null ? LocalDate.now(BUSINESS_ZONE).plusYears(10) : toDateOf(request);

        return bugRepository.findBugExportRows(
                projectId,
                fromDate.atStartOfDay(BUSINESS_ZONE).toInstant(),
                toDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant(),
                sprintIdOf(request),
                userIdOf(request),
                null,
                null,
                null
        );
    }

    private boolean matchesUserFilter(Task task, UUID userId) {
        return userId == null
                || userId.equals(task.getAssigneeUserId())
                || userId.equals(task.getReporterUserId());
    }

    private boolean matchesDateRange(Task task, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return true;
        }

        LocalDate candidate = task.getStartDate() != null
                ? task.getStartDate()
                : task.getDueDate();

        if (candidate == null) {
            return true;
        }

        if (fromDate != null && candidate.isBefore(fromDate)) {
            return false;
        }

        return toDate == null || !candidate.isAfter(toDate);
    }

    private void validateProjectReportRequest(UUID projectId, ProjectExcelReportRequest request) {
        if (request == null) {
            return;
        }

        if (request.fromDate() != null
                && request.toDate() != null
                && request.fromDate().isAfter(request.toDate())) {
            throw new BusinessException(ErrorCode.REPORT_DATE_RANGE_INVALID);
        }

        if (request.sprintId() != null) {
            getSprintOrThrow(projectId, request.sprintId());
        }

        if (request.userId() != null
                && !projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new BusinessException(ErrorCode.REPORT_USER_NOT_PROJECT_MEMBER);
        }
    }

    private Sprint getSprintOrThrow(UUID projectId, UUID sprintId) {
        return sprintRepository.findByIdAndProjectId(sprintId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPRINT_NOT_FOUND));
    }

    private GeneratedReportFile buildFile(XSSFWorkbook workbook, String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        return new GeneratedReportFile(
                excelReportHelper.safeFileName(fileName),
                ReportContentTypes.EXCEL_XLSX,
                outputStream.toByteArray()
        );
    }

    private UUID sprintIdOf(ProjectExcelReportRequest request) {
        return request == null ? null : request.sprintId();
    }

    private UUID userIdOf(ProjectExcelReportRequest request) {
        return request == null ? null : request.userId();
    }

    private LocalDate fromDateOf(ProjectExcelReportRequest request) {
        return request == null ? null : request.fromDate();
    }

    private LocalDate toDateOf(ProjectExcelReportRequest request) {
        return request == null ? null : request.toDate();
    }

    private boolean isOpenBug(BugExportRowView bug) {
        return bug.getStatus() != BugStatus.RESOLVED
                && bug.getStatus() != BugStatus.VERIFIED
                && bug.getStatus() != BugStatus.CLOSED
                && bug.getStatus() != BugStatus.CANCELLED;
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private String displayUser(String username, String email) {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email == null ? "" : email;
    }
}
