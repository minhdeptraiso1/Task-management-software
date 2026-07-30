package com.project.taskmanagement.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.project.taskmanagement.dto.request.report.ProjectExcelReportRequest;
import com.project.taskmanagement.dto.response.analytics.BurnupChartResponse;
import com.project.taskmanagement.dto.response.analytics.CumulativeFlowResponse;
import com.project.taskmanagement.dto.response.analytics.VelocityChartResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintBurndownResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;
import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import com.project.taskmanagement.repository.TaskTimeLogRepository;
import com.project.taskmanagement.repository.projection.report.ReportMemberPerformanceExcelView;
import com.project.taskmanagement.repository.projection.report.ReportTimeLogExcelView;
import com.project.taskmanagement.service.AnalyticsService;
import com.project.taskmanagement.service.ReportPdfExportService;
import com.project.taskmanagement.service.TaskStatisticsService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.GeneratedReportFile;
import com.project.taskmanagement.service.report.ReportContentTypes;
import com.project.taskmanagement.service.report.ReportFileNameBuilder;
import com.project.taskmanagement.service.report.pdf.PdfChartHelper;
import com.project.taskmanagement.service.report.pdf.PdfPageNumberEventHandler;
import com.project.taskmanagement.service.report.pdf.PdfReportHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.taskmanagement.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportPdfExportServiceImpl implements ReportPdfExportService {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    SprintRepository sprintRepository;
    BacklogItemRepository backlogItemRepository;
    TaskRepository taskRepository;
    TaskTimeLogRepository taskTimeLogRepository;
    ProjectMemberRepository projectMemberRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;
    TaskStatisticsService taskStatisticsService;
    PdfReportHelper pdfReportHelper;
    PdfChartHelper pdfChartHelper;
    UserRepository userRepository;
    AnalyticsService analyticsService;
    ReportFileNameBuilder reportFileNameBuilder;

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportSprintReportPdf(UUID projectId, UUID sprintId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);

        Sprint sprint = getSprintOrThrow(projectId, sprintId);
        SprintTaskStatisticsResponse statistics = taskStatisticsService.getSprintStatistics(projectId, sprintId);
        SprintBurndownResponse burndown = taskStatisticsService.getSprintBurndown(projectId, sprintId);
        List<BacklogItem> backlogItems = backlogItemRepository
                .findAllByProjectIdAndSprintIdOrderByPositionAsc(projectId, sprintId);
        List<Task> tasks = taskRepository
                .findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(projectId, sprintId);
        List<ReportTimeLogExcelView> timeLogs = taskTimeLogRepository
                .findTimeLogsForExcelReport(projectId, sprintId, null, null, null);
        List<ReportMemberPerformanceExcelView> memberPerformance = taskRepository
                .findMemberPerformanceForExcelReport(projectId, sprintId, null, null, null, LocalDate.now(BUSINESS_ZONE));

        return buildPdf(buildSprintPdfFileName(project, sprint), document -> {
            document.add(pdfReportHelper.title("BÁO CÁO CHI TIẾT SPRINT"));
            document.add(pdfReportHelper.generatedAt());
            addProjectSection(document, project);
            addSprintSection(document, sprint);
            addSprintStatisticsSection(document, statistics);
            addBacklogSummarySection(document, backlogItems);
            addTimeSummarySection(document, timeLogs);
            addMemberPerformanceSection(document, memberPerformance);
            addBurndownDataSection(document, burndown);
            addBurnupSection(document, projectId, sprintId);
            addCumulativeFlowSection(document, projectId, sprintId);
            addTaskSampleSection(document, tasks);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile exportProjectReportPdf(UUID projectId, ProjectExcelReportRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        validateProjectReportRequest(projectId, request);

        List<Sprint> sprints = sprintRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId);
        List<BacklogItem> backlogItems = loadProjectBacklogItems(projectId, request);
        List<Task> tasks = loadProjectTasks(projectId, request);
        List<ReportTimeLogExcelView> timeLogs = taskTimeLogRepository.findTimeLogsForExcelReport(
                projectId,
                sprintIdOf(request),
                userIdOf(request),
                fromDateOf(request),
                toDateOf(request)
        );
        List<ReportMemberPerformanceExcelView> memberPerformance = taskRepository.findMemberPerformanceForExcelReport(
                projectId,
                sprintIdOf(request),
                userIdOf(request),
                fromDateOf(request),
                toDateOf(request),
                LocalDate.now(BUSINESS_ZONE)
        );

        return buildPdf(buildProjectPdfFileName(project), document -> {
            document.add(pdfReportHelper.title("BÁO CÁO TỔNG QUAN DỰ ÁN"));
            document.add(pdfReportHelper.generatedAt());
            addProjectSection(document, project);
            addProjectFilterSection(document, request);
            addProjectSummarySection(document, sprints, backlogItems, tasks);
            addVelocitySection(document, projectId);
            if (sprintIdOf(request) != null) {
                addBurnupSection(document, projectId, sprintIdOf(request));
                addCumulativeFlowSection(document, projectId, sprintIdOf(request));
            }
            addTimeSummarySection(document, timeLogs);
            addMemberPerformanceSection(document, memberPerformance);
            addTaskSampleSection(document, tasks);
        });
    }

    private void addProjectSection(Document document, Project project) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Thông tin Dự án"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Mã Dự án", project.getCode());
        pdfReportHelper.addKeyValueRow(table, "Tên Dự án", project.getName());
        pdfReportHelper.addKeyValueRow(table, "Trạng thái", project.getStatus());
        pdfReportHelper.addKeyValueRow(table, "Ngày bắt đầu", project.getStartDate());
        pdfReportHelper.addKeyValueRow(table, "Ngày kết thúc", project.getEndDate());

        document.add(table);
    }

    private void addBurndownDataSection(Document document, SprintBurndownResponse burndown) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Dữ liệu Biểu đồ Burndown"));
        document.add(pdfChartHelper.burndownDataTable(burndown == null ? null : burndown.points()));
    }

    private void addVelocitySection(Document document, UUID projectId) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Tốc độ Hoàn thành (Velocity)"));

        VelocityChartResponse velocity = analyticsService.getVelocity(projectId);
        PdfPTable table = pdfReportHelper.table(4);
        pdfReportHelper.addHeaderCell(table, "Sprint");
        pdfReportHelper.addHeaderCell(table, "SP Cam kết");
        pdfReportHelper.addHeaderCell(table, "SP Hoàn thành");
        pdfReportHelper.addHeaderCell(table, "Tỷ lệ (%)");

        velocity.points().stream()
                .limit(20)
                .forEach(item -> {
                    pdfReportHelper.addCell(table, item.sprintName());
                    pdfReportHelper.addCell(table, item.committedStoryPoints());
                    pdfReportHelper.addCell(table, item.completedStoryPoints());
                    pdfReportHelper.addCell(table, item.completionRate() + "%");
                });

        document.add(table);
    }

    private void addBurnupSection(Document document, UUID projectId, UUID sprintId) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Tiến độ Phạm vi (Burnup)"));

        BurnupChartResponse burnup = analyticsService.getSprintBurnup(projectId, sprintId);
        PdfPTable table = pdfReportHelper.table(4);
        pdfReportHelper.addHeaderCell(table, "Mốc thời gian");
        pdfReportHelper.addHeaderCell(table, "Tổng phạm vi");
        pdfReportHelper.addHeaderCell(table, "Đã hoàn thành");
        pdfReportHelper.addHeaderCell(table, "Tỷ lệ (%)");

        burnup.points().stream()
                .limit(40)
                .forEach(point -> {
                    pdfReportHelper.addCell(table, point.date());
                    pdfReportHelper.addCell(table, point.totalScope());
                    pdfReportHelper.addCell(table, point.completedScope());
                    pdfReportHelper.addCell(table, point.completionRate() + "%");
                });

        document.add(table);
    }

    private void addCumulativeFlowSection(Document document, UUID projectId, UUID sprintId) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Dòng chảy Tích lũy (Cumulative Flow)"));

        CumulativeFlowResponse cumulativeFlow = analyticsService.getSprintCumulativeFlow(projectId, sprintId);
        PdfPTable table = pdfReportHelper.table(7);
        pdfReportHelper.addHeaderCell(table, "Mốc thời gian");
        pdfReportHelper.addHeaderCell(table, "Cần làm");
        pdfReportHelper.addHeaderCell(table, "Đang làm");
        pdfReportHelper.addHeaderCell(table, "Đang review");
        pdfReportHelper.addHeaderCell(table, "Nghẽn");
        pdfReportHelper.addHeaderCell(table, "Đã xong");
        pdfReportHelper.addHeaderCell(table, "Đã hủy");

        cumulativeFlow.points().stream()
                .limit(40)
                .forEach(point -> {
                    pdfReportHelper.addCell(table, point.date());
                    pdfReportHelper.addCell(table, point.todo());
                    pdfReportHelper.addCell(table, point.inProgress());
                    pdfReportHelper.addCell(table, point.inReview());
                    pdfReportHelper.addCell(table, point.blocked());
                    pdfReportHelper.addCell(table, point.done());
                    pdfReportHelper.addCell(table, point.cancelled());
                });

        document.add(table);
    }

    private void addSprintSection(Document document, Sprint sprint) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Thông tin Sprint"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Tên Sprint", sprint.getName());
        pdfReportHelper.addKeyValueRow(table, "Mục tiêu", sprint.getGoal());
        pdfReportHelper.addKeyValueRow(table, "Trạng thái", sprint.getStatus());
        pdfReportHelper.addKeyValueRow(table, "Ngày bắt đầu", sprint.getStartDate());
        pdfReportHelper.addKeyValueRow(table, "Ngày kết thúc", sprint.getEndDate());
        pdfReportHelper.addKeyValueRow(table, "Bắt đầu thực tế", sprint.getStartedAt());
        pdfReportHelper.addKeyValueRow(table, "Hoàn thành thực tế", sprint.getCompletedAt());

        document.add(table);
    }

    private void addSprintStatisticsSection(Document document, SprintTaskStatisticsResponse statistics) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Thống kê Chỉ số Sprint"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Tổng số Công việc (Tasks)", statistics.totalTasks());
        pdfReportHelper.addKeyValueRow(table, "Công việc đã hoàn thành", statistics.completedTasks());
        pdfReportHelper.addKeyValueRow(table, "Công việc chưa xong", statistics.unfinishedTasks());
        pdfReportHelper.addKeyValueRow(table, "Công việc đang chờ (Blocked)", statistics.blockedTasks());
        pdfReportHelper.addKeyValueRow(table, "Công việc trễ hạn (Overdue)", statistics.overdueTasks());
        pdfReportHelper.addKeyValueRow(table, "Tỷ lệ hoàn thành", statistics.completionRate() + "%");
        pdfReportHelper.addKeyValueRow(table, "Thời gian ước lượng", pdfReportHelper.minutesToHourText(statistics.estimatedMinutes()));
        pdfReportHelper.addKeyValueRow(table, "Thời gian thực tế", pdfReportHelper.minutesToHourText(statistics.spentMinutes()));

        document.add(table);
    }

    private void addProjectFilterSection(Document document, ProjectExcelReportRequest request) throws Exception {
        if (request == null) {
            return;
        }

        document.add(pdfReportHelper.sectionTitle("Bộ lọc Báo cáo"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Từ ngày", request.fromDate());
        pdfReportHelper.addKeyValueRow(table, "Đến ngày", request.toDate());
        pdfReportHelper.addKeyValueRow(table, "Mã Sprint", request.sprintId());
        pdfReportHelper.addKeyValueRow(table, "Mã Thành viên", request.userId());

        document.add(table);
    }

    private void addProjectSummarySection(
            Document document,
            List<Sprint> sprints,
            List<BacklogItem> backlogItems,
            List<Task> tasks
    ) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Tổng quan Dự án"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Tổng số Sprint", sprints.size());
        pdfReportHelper.addKeyValueRow(table, "Tổng Hạng mục Backlog", backlogItems.size());
        pdfReportHelper.addKeyValueRow(table, "Tổng số Công việc (Tasks)", tasks.size());
        pdfReportHelper.addKeyValueRow(table, "Task đã hoàn thành", countTasksByStatus(tasks, TaskStatus.DONE));
        pdfReportHelper.addKeyValueRow(table, "Task đang chờ", countTasksByStatus(tasks, TaskStatus.BLOCKED));
        pdfReportHelper.addKeyValueRow(table, "Task trễ hạn", countOverdueTasks(tasks));
        pdfReportHelper.addKeyValueRow(table, "Thời gian ước lượng", pdfReportHelper.minutesToHourText(sumEstimatedMinutes(tasks)));

        document.add(table);
    }

    private void addBacklogSummarySection(Document document, List<BacklogItem> backlogItems) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Tổng quan Hạng mục Backlog"));

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Tổng Hạng mục Backlog", backlogItems.size());
        pdfReportHelper.addKeyValueRow(table, "Tổng điểm Story Points", backlogItems.stream()
                .mapToLong(item -> safeLong(item.getStoryPoints()))
                .sum());

        document.add(table);
    }

    private void addTimeSummarySection(Document document, List<ReportTimeLogExcelView> timeLogs) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Tổng quan Thời gian Log"));

        long totalMinutes = timeLogs.stream()
                .mapToLong(item -> safeLong(item.getMinutes()))
                .sum();

        PdfPTable table = pdfReportHelper.keyValueTable();
        pdfReportHelper.addKeyValueRow(table, "Tổng số bản ghi TimeLog", timeLogs.size());
        pdfReportHelper.addKeyValueRow(table, "Tổng số phút", totalMinutes);
        pdfReportHelper.addKeyValueRow(table, "Tổng thời gian", pdfReportHelper.minutesToHourText(totalMinutes));

        document.add(table);
    }

    private void addMemberPerformanceSection(
            Document document,
            List<ReportMemberPerformanceExcelView> memberPerformance
    ) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Hiệu suất Thành viên"));

        PdfPTable table = pdfReportHelper.table(5);
        pdfReportHelper.addHeaderCell(table, "Thành viên");
        pdfReportHelper.addHeaderCell(table, "Tổng Task");
        pdfReportHelper.addHeaderCell(table, "Đã xong");
        pdfReportHelper.addHeaderCell(table, "Đang chờ");
        pdfReportHelper.addHeaderCell(table, "Đã log");

        memberPerformance.stream()
                .limit(20)
                .forEach(member -> {
                    pdfReportHelper.addCell(table, displayUser(member.getUsername(), member.getEmail()));
                    pdfReportHelper.addCell(table, safeLong(member.getTotalTasks()));
                    pdfReportHelper.addCell(table, safeLong(member.getDoneTasks()));
                    pdfReportHelper.addCell(table, safeLong(member.getBlockedTasks()));
                    pdfReportHelper.addCell(table, pdfReportHelper.minutesToHourText(member.getSpentMinutes()));
                });

        document.add(table);
    }

    private void addTaskSampleSection(Document document, List<Task> tasks) throws Exception {
        document.add(pdfReportHelper.sectionTitle("Danh sách Công việc"));

        PdfPTable table = pdfReportHelper.table(5);
        pdfReportHelper.addHeaderCell(table, "Tiêu đề Task");
        pdfReportHelper.addHeaderCell(table, "Trạng thái");
        pdfReportHelper.addHeaderCell(table, "Độ ưu tiên");
        pdfReportHelper.addHeaderCell(table, "Người thực hiện");
        pdfReportHelper.addHeaderCell(table, "Hạn chót");

        Map<UUID, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        tasks.stream()
                .limit(30)
                .forEach(task -> {
                    String assigneeName = task.getAssigneeUserId() != null && userMap.containsKey(task.getAssigneeUserId())
                            ? userMap.get(task.getAssigneeUserId()).getUsername() : "Chưa giao";
                    pdfReportHelper.addCell(table, task.getTitle());
                    pdfReportHelper.addCell(table, task.getStatus());
                    pdfReportHelper.addCell(table, task.getPriority());
                    pdfReportHelper.addCell(table, assigneeName);
                    pdfReportHelper.addCell(table, task.getDueDate());
                });

        document.add(table);

        if (tasks.size() > 30) {
            document.add(pdfReportHelper.normalText(
                    "Only first 30 tasks are shown in this foundation report. Full detail remains available in Excel export."
            ));
        }
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

    private boolean matchesUserFilter(Task task, UUID userId) {
        return userId == null
                || userId.equals(task.getAssigneeUserId())
                || userId.equals(task.getReporterUserId());
    }

    private boolean matchesDateRange(Task task, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return true;
        }

        LocalDate candidate = task.getStartDate() != null ? task.getStartDate() : task.getDueDate();
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

    private GeneratedReportFile buildPdf(String fileName, PdfDocumentWriter writer) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = pdfReportHelper.createDocument();
            PdfWriter writerInstance = PdfWriter.getInstance(document, outputStream);
            writerInstance.setPageEvent(new PdfPageNumberEventHandler(pdfReportHelper));

            document.open();
            writer.write(document);
            document.close();

            return new GeneratedReportFile(
                    pdfReportHelper.safeFileName(fileName),
                    ReportContentTypes.PDF,
                    outputStream.toByteArray()
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.REPORT_PDF_EXPORT_FAILED);
        }
    }

    private String buildSprintPdfFileName(Project project, Sprint sprint) {
        return reportFileNameBuilder.sprintPdf(project, sprint);
    }

    private String buildProjectPdfFileName(Project project) {
        return reportFileNameBuilder.projectPdf(project);
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

    private long countTasksByStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    private long countOverdueTasks(List<Task> tasks) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .count();
    }

    private long sumEstimatedMinutes(List<Task> tasks) {
        return tasks.stream()
                .mapToLong(task -> safeLong(task.getEstimatedMinutes()))
                .sum();
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

    @FunctionalInterface
    private interface PdfDocumentWriter {
        void write(Document document) throws Exception;
    }
}
