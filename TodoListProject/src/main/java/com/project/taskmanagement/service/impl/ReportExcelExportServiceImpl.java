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
import com.project.taskmanagement.repository.UserRepository;
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
import java.util.Map;
import java.util.stream.Collectors;
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
    UserRepository userRepository;
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

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportProjectTimeLogsReport(UUID projectId, ProjectExcelReportRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        validateProjectReportRequest(projectId, request);

        List<ReportTimeLogExcelView> rows = loadTimeLogRows(
                projectId,
                sprintIdOf(request),
                userIdOf(request),
                fromDateOf(request),
                toDateOf(request)
        );

        try (XSSFWorkbook workbook = excelReportHelper.createWorkbook()) {
            createTimeLogSummarySheet(workbook, project, request, rows);
            createTimeLogsSheet(workbook, rows);

            return buildFile(workbook, reportFileNameBuilder.projectTimeLogsExcel(project));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportMyTimeLogsReport(ProjectExcelReportRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();

        List<ReportTimeLogExcelView> rows = loadTimeLogRows(
                null,
                sprintIdOf(request),
                currentUser.getId(),
                fromDateOf(request),
                toDateOf(request)
        );

        try (XSSFWorkbook workbook = excelReportHelper.createWorkbook()) {
            createTimeLogSummarySheet(workbook, null, request, rows);
            createTimeLogsSheet(workbook, rows);

            return buildFile(workbook, reportFileNameBuilder.personalTimeLogsExcel(currentUser));
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
        excelReportHelper.createHeaderRow(sheet, "Chỉ số", "Giá trị");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Mã Dự án", project.getCode());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tên Dự án", project.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tên Sprint", sprint.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Trạng thái Sprint", sprint.getStatus());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Ngày bắt đầu", sprint.getStartDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Ngày kết thúc", sprint.getEndDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Công việc (Tasks)", statistics.totalTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Công việc đã hoàn thành", statistics.completedTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Công việc đang chờ (Blocked)", statistics.blockedTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Công việc trễ hạn (Overdue)", statistics.overdueTasks());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tỷ lệ hoàn thành (%)", statistics.completionRate() + "%");
        rowIndex = addKeyValueRow(sheet, rowIndex, "Thời gian ước lượng", excelReportHelper.minutesToHourText(statistics.estimatedMinutes()));
        addKeyValueRow(sheet, rowIndex, "Thời gian đã thực hiện", excelReportHelper.minutesToHourText(statistics.spentMinutes()));

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
        excelReportHelper.createHeaderRow(sheet, "Chỉ số Tổng quan", "Giá trị");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Mã Dự án", project.getCode());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tên Dự án", project.getName());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Trạng thái Dự án", project.getStatus());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Ngày bắt đầu", project.getStartDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Ngày kết thúc", project.getEndDate());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Sprint", sprintCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Hạng mục Backlog", backlogItemCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Công việc (Tasks)", taskCount);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Lỗi Bug QA", bugCount);

        if (request != null) {
            rowIndex = addKeyValueRow(sheet, rowIndex, "Lọc Từ ngày", request.fromDate());
            rowIndex = addKeyValueRow(sheet, rowIndex, "Lọc Đến ngày", request.toDate());
            rowIndex = addKeyValueRow(sheet, rowIndex, "Lọc theo Mã Sprint", request.sprintId());
            addKeyValueRow(sheet, rowIndex, "Lọc theo Mã Thành viên", request.userId());
        }

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createSprintsSheet(XSSFWorkbook workbook, List<Sprint> sprints) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.SPRINTS);
        int columnCount = 9;
        excelReportHelper.createHeaderRow(
                sheet,
                "Mã Sprint (ID)",
                "Tên Sprint",
                "Mục tiêu Sprint",
                "Trạng thái",
                "Ngày bắt đầu",
                "Ngày kết thúc",
                "Thực tế bắt đầu",
                "Thực tế hoàn thành",
                "Ngày tạo"
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
                "Mã Hạng mục (ID)",
                "Mã Sprint",
                "Tiêu đề Hạng mục",
                "Loại Hạng mục",
                "Trạng thái",
                "Độ ưu tiên",
                "Story Points",
                "Thứ tự",
                "Ngày tạo"
        );

        Map<UUID, Sprint> sprintMap = sprintRepository.findAll().stream()
                .collect(Collectors.toMap(Sprint::getId, s -> s, (a, b) -> a));

        int rowIndex = 1;
        for (BacklogItem item : backlogItems) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            String sprintName = item.getSprintId() != null && sprintMap.containsKey(item.getSprintId())
                    ? sprintMap.get(item.getSprintId()).getName() : "Product Backlog";

            excelReportHelper.setCell(row, column++, item.getId());
            excelReportHelper.setCell(row, column++, sprintName);
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
                "Mã Công việc (ID)",
                "Hạng mục Backlog",
                "Tên Sprint",
                "Tiêu đề Công việc",
                "Loại Task",
                "Độ ưu tiên",
                "Trạng thái",
                "Người thực hiện",
                "Người tạo",
                "Thời gian ước lượng (phút)",
                "Ngày bắt đầu",
                "Hạn chót",
                "Hoàn thành lúc",
                "Thứ tự",
                "Ngày tạo"
        );

        Map<UUID, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<UUID, Sprint> sprintMap = sprintRepository.findAll().stream()
                .collect(Collectors.toMap(Sprint::getId, s -> s, (a, b) -> a));
        Map<UUID, BacklogItem> backlogItemMap = backlogItemRepository.findAll().stream()
                .collect(Collectors.toMap(BacklogItem::getId, b -> b, (a, b) -> a));

        int rowIndex = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            String backlogTitle = task.getBacklogItemId() != null && backlogItemMap.containsKey(task.getBacklogItemId())
                    ? backlogItemMap.get(task.getBacklogItemId()).getTitle() : "—";
            String sprintName = task.getCurrentSprintId() != null && sprintMap.containsKey(task.getCurrentSprintId())
                    ? sprintMap.get(task.getCurrentSprintId()).getName() : "Backlog";
            String assigneeName = task.getAssigneeUserId() != null && userMap.containsKey(task.getAssigneeUserId())
                    ? userMap.get(task.getAssigneeUserId()).getUsername() : "Chưa giao";
            String reporterName = task.getReporterUserId() != null && userMap.containsKey(task.getReporterUserId())
                    ? userMap.get(task.getReporterUserId()).getUsername() : "—";

            excelReportHelper.setCell(row, column++, task.getId());
            excelReportHelper.setCell(row, column++, backlogTitle);
            excelReportHelper.setCell(row, column++, sprintName);
            excelReportHelper.setCell(row, column++, task.getTitle());
            excelReportHelper.setCell(row, column++, task.getType());
            excelReportHelper.setCell(row, column++, task.getPriority());
            excelReportHelper.setCell(row, column++, task.getStatus());
            excelReportHelper.setCell(row, column++, assigneeName);
            excelReportHelper.setCell(row, column++, reporterName);
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
        createTimeLogsSheet(workbook, loadTimeLogRows(projectId, sprintId, userId, fromDate, toDate));
    }

    private void createTimeLogSummarySheet(
            XSSFWorkbook workbook,
            Project project,
            ProjectExcelReportRequest request,
            List<ReportTimeLogExcelView> rows
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.TIME_LOG_SUMMARY);
        excelReportHelper.createHeaderRow(sheet, "Chỉ số", "Giá trị");

        long totalMinutes = rows.stream()
                .map(ReportTimeLogExcelView::getMinutes)
                .mapToLong(this::safeLong)
                .sum();

        int rowIndex = 1;
        if (project != null) {
            rowIndex = addKeyValueRow(sheet, rowIndex, "Dự án", project.getName());
            rowIndex = addKeyValueRow(sheet, rowIndex, "Mã dự án", project.getCode());
        } else {
            rowIndex = addKeyValueRow(sheet, rowIndex, "Phạm vi báo cáo", "Tất cả dự án cá nhân");
        }
        rowIndex = addKeyValueRow(sheet, rowIndex, "Từ ngày", fromDateOf(request));
        rowIndex = addKeyValueRow(sheet, rowIndex, "Đến ngày", toDateOf(request));
        rowIndex = addKeyValueRow(sheet, rowIndex, "Mã Sprint", sprintIdOf(request));
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tên thành viên", memberNameOf(request, rows));
        rowIndex = addKeyValueRow(sheet, rowIndex, "Email thành viên", memberEmailOf(request, rows));
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số dòng log", rows.size());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số phút", totalMinutes);
        addKeyValueRow(sheet, rowIndex, "Tổng thời gian", excelReportHelper.minutesToHourText(totalMinutes));

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createTimeLogsSheet(
            XSSFWorkbook workbook,
            List<ReportTimeLogExcelView> rows
    ) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.TIME_LOGS);
        int columnCount = 11;
        excelReportHelper.createHeaderRow(
                sheet,
                "Mã TimeLog (ID)",
                "Tên Dự án",
                "Tiêu đề Công việc",
                "Tên Sprint",
                "Hạng mục Backlog",
                "Thành viên",
                "Email",
                "Ngày làm việc",
                "Số phút",
                "Thời gian (giờ)",
                "Mô tả công việc"
        );

        int rowIndex = 1;
        for (ReportTimeLogExcelView item : rows) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, item.getTimeLogId());
            excelReportHelper.setCell(row, column++, item.getProjectName());
            excelReportHelper.setCell(row, column++, item.getTaskTitle());
            excelReportHelper.setCell(row, column++, item.getSprintName() != null ? item.getSprintName() : "—");
            excelReportHelper.setCell(row, column++, item.getBacklogItemTitle() != null ? item.getBacklogItemTitle() : "—");
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
        int columnCount = 11;
        excelReportHelper.createHeaderRow(
                sheet,
                "Tên người dùng",
                "Email",
                "Tổng số Task",
                "Task đã xong",
                "Task đang làm",
                "Task đang chờ",
                "Task trễ hạn",
                "Thời gian ước lượng (phút)",
                "Ước lượng (giờ)",
                "Thời gian đã log (phút)",
                "Đã log (giờ)"
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
        excelReportHelper.createMemberPerformanceChart(sheet, rowIndex);
    }

    private void createBugSummarySheet(XSSFWorkbook workbook, List<BugExportRowView> bugs) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BUG_SUMMARY);
        excelReportHelper.createHeaderRow(sheet, "Chỉ số Bug QA", "Giá trị");

        int rowIndex = 1;
        rowIndex = addKeyValueRow(sheet, rowIndex, "Tổng số Bug", bugs.size());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug đang mở (Open)", bugs.stream().filter(this::isOpenBug).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug đã giải quyết (Resolved)", bugs.stream().filter(item -> item.getStatus() == BugStatus.RESOLVED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug đã đóng (Closed)", bugs.stream().filter(item -> item.getStatus() == BugStatus.CLOSED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug đã xác minh (Verified)", bugs.stream().filter(item -> item.getStatus() == BugStatus.VERIFIED).count());
        rowIndex = addKeyValueRow(sheet, rowIndex, "Bug nguy cấp (Critical)", bugs.stream().filter(item -> item.getSeverity() == BugSeverity.CRITICAL).count());
        addKeyValueRow(sheet, rowIndex, "Tổng số lần mở lại", bugs.stream().mapToLong(item -> safeLong(item.getReopenedCount())).sum());

        excelReportHelper.autoSizeColumns(sheet, 2);
    }

    private void createBugListSheet(XSSFWorkbook workbook, List<BugExportRowView> bugs) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BUG_LIST);
        int columnCount = 17;
        excelReportHelper.createHeaderRow(
                sheet,
                "Mã Bug (ID)",
                "Tiêu đề Bug",
                "Trạng thái",
                "Mức độ nghiêm trọng",
                "Độ ưu tiên",
                "Tên Sprint",
                "Công việc liên quan",
                "Hạng mục Backlog",
                "Người được giao",
                "Người báo lỗi",
                "Hạn chót",
                "Số lần mở lại",
                "Giải quyết lúc",
                "Đóng lúc",
                "Tạo lúc",
                "Cập nhật lúc",
                "Mô tả chi tiết"
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
                "Mã Sprint",
                "Tên Sprint",
                "Trạng thái Sprint",
                "Ngày bắt đầu",
                "Ngày kết thúc",
                "Số mục cam kết",
                "Số mục hoàn thành",
                "Story Points cam kết",
                "Story Points hoàn thành",
                "Tỷ lệ hoàn thành (%)"
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
        excelReportHelper.createVelocityChart(sheet, rowIndex);
    }

    private void createBurnupSheet(XSSFWorkbook workbook, UUID projectId, UUID sprintId) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.BURNUP);
        int columnCount = 4;
        excelReportHelper.createHeaderRow(
                sheet,
                "Mốc thời gian",
                "Tổng phạm vi",
                "Đã hoàn thành",
                "Tỷ lệ hoàn thành (%)"
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
        excelReportHelper.createBurnupChart(sheet, rowIndex);
    }

    private void createCumulativeFlowSheet(XSSFWorkbook workbook, UUID projectId, UUID sprintId) {
        Sheet sheet = workbook.createSheet(ReportSheetNames.CUMULATIVE_FLOW);
        int columnCount = 7;
        excelReportHelper.createHeaderRow(
                sheet,
                "Mốc thời gian",
                "Cần làm",
                "Đang làm",
                "Đang chờ",
                "Đang review",
                "Hoàn thành",
                "Đã hủy"
        );

        CumulativeFlowResponse cumulativeFlow = analyticsService.getSprintCumulativeFlow(projectId, sprintId);
        int rowIndex = 1;
        for (var point : cumulativeFlow.points()) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            excelReportHelper.setCell(row, column++, point.date());
            excelReportHelper.setCell(row, column++, point.todo());
            excelReportHelper.setCell(row, column++, point.inProgress());
            excelReportHelper.setCell(row, column++, point.blocked());
            excelReportHelper.setCell(row, column++, point.inReview());
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

    private List<ReportTimeLogExcelView> loadTimeLogRows(
            UUID projectId,
            UUID sprintId,
            UUID userId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return taskTimeLogRepository.findTimeLogsForExcelReport(
                projectId,
                sprintId,
                userId,
                fromDate,
                toDate
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

    private String memberNameOf(ProjectExcelReportRequest request, List<ReportTimeLogExcelView> rows) {
        UUID userId = userIdOf(request);
        if (userId == null) {
            if (!rows.isEmpty() && rows.get(0).getUsername() != null && !rows.get(0).getUsername().isBlank()) {
                return rows.get(0).getUsername();
            }
            User currentUser = currentUserService.getActiveCurrentUser();
            if (currentUser != null) {
                return currentUser.getUsername();
            }
            return "Tất cả thành viên";
        }

        return rows.stream()
                .filter(item -> userId.equals(item.getUserId()))
                .map(ReportTimeLogExcelView::getUsername)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseGet(() -> userRepository.findById(userId)
                        .map(User::getUsername)
                        .orElse(""));
    }

    private String memberEmailOf(ProjectExcelReportRequest request, List<ReportTimeLogExcelView> rows) {
        UUID userId = userIdOf(request);
        if (userId == null) {
            if (!rows.isEmpty() && rows.get(0).getEmail() != null && !rows.get(0).getEmail().isBlank()) {
                return rows.get(0).getEmail();
            }
            User currentUser = currentUserService.getActiveCurrentUser();
            if (currentUser != null) {
                return currentUser.getEmail();
            }
            return "Tất cả thành viên";
        }

        return rows.stream()
                .filter(item -> userId.equals(item.getUserId()))
                .map(ReportTimeLogExcelView::getEmail)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseGet(() -> userRepository.findById(userId)
                        .map(User::getEmail)
                        .orElse(""));
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
