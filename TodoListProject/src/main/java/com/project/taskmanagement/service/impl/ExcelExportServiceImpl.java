package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.bug.BugReportExportRequest;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BugRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.projection.bug.BugExportRowView;
import com.project.taskmanagement.service.BugExportService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.GeneratedExcelFile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExcelExportServiceImpl implements BugExportService {

    BugRepository bugRepository;
    SprintRepository sprintRepository;
    ProjectMemberRepository projectMemberRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    @Transactional(readOnly = true)
    public GeneratedExcelFile exportBugReport(UUID projectId, BugReportExportRequest request) {
        Project project = requireProjectViewAccess(projectId);
        DateRange dateRange = resolveDateRange(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        validateOptionalSprint(projectId, request == null ? null : request.sprintId());
        validateOptionalProjectMember(projectId, request == null ? null : request.assigneeUserId());

        List<BugExportRowView> rows = bugRepository.findBugExportRows(
                projectId,
                dateRange.fromDate().atStartOfDay(BUSINESS_ZONE).toInstant(),
                dateRange.toDate().plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant(),
                request == null ? null : request.sprintId(),
                request == null ? null : request.assigneeUserId(),
                request == null ? null : request.status(),
                request == null ? null : request.severity(),
                request == null ? null : request.priority()
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = createStyles(workbook);
            createBugSummarySheet(workbook, styles, project, dateRange, rows);
            createBugListSheet(workbook, styles, rows);
            createCountSheet(workbook, styles, "BUG_BY_STATUS", rows.stream()
                    .collect(Collectors.groupingBy(row -> label(row.getStatus()), LinkedHashMap::new, Collectors.counting())));
            createCountSheet(workbook, styles, "BUG_BY_SEVERITY", rows.stream()
                    .collect(Collectors.groupingBy(row -> label(row.getSeverity()), LinkedHashMap::new, Collectors.counting())));
            createBugAssigneeSheet(workbook, styles, rows);
            createQaMetricsSheet(workbook, styles, rows);
            workbook.write(out);
            return file("bug-report-" + project.getCode() + "-" + nowFilePart() + ".xlsx", out.toByteArray());
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUG_EXPORT_FAILED);
        }
    }

    private void createBugSummarySheet(Workbook workbook, Styles styles, Project project, DateRange range, List<BugExportRowView> rows) {
        Sheet sheet = workbook.createSheet("BUG_SUMMARY");
        int row = 0;
        row = writeKeyValue(sheet, row, styles.header(), "Dự án", project.getCode() + " - " + project.getName());
        row = writeKeyValue(sheet, row, styles.header(), "Từ ngày", range.fromDate().toString());
        row = writeKeyValue(sheet, row, styles.header(), "Đến ngày", range.toDate().toString());
        row = writeKeyValue(sheet, row, styles.header(), "Tổng số Lỗi Bug", String.valueOf(rows.size()));
        row = writeKeyValue(sheet, row, styles.header(), "Bug đã giải quyết / đóng", String.valueOf(rows.stream()
                .filter(item -> item.getStatus() == BugStatus.RESOLVED
                        || item.getStatus() == BugStatus.VERIFIED
                        || item.getStatus() == BugStatus.CLOSED)
                .count()));
        row = writeKeyValue(sheet, row, styles.header(), "Bug nguy cấp (Critical)", String.valueOf(rows.stream()
                .filter(item -> item.getSeverity() == BugSeverity.CRITICAL)
                .count()));
        writeKeyValue(sheet, row, styles.header(), "Bug trễ hạn (Overdue)", String.valueOf(countOverdueBugs(rows)));
        autoSize(sheet, 2);
    }

    private void createBugListSheet(Workbook workbook, Styles styles, List<BugExportRowView> rows) {
        Sheet sheet = workbook.createSheet("BUG_LIST");
        String[] headers = {"Mã Bug (ID)", "Tiêu đề Bug", "Trạng thái", "Mức độ nghiêm trọng", "Độ ưu tiên", "Tên Sprint", "Tiêu đề Task", "Hạng mục Backlog",
                "Người được giao", "Người báo lỗi", "Hạn chót", "Số lần mở lại", "Giải quyết lúc", "Đóng lúc", "Tạo lúc", "Cập nhật lúc", "Mô tả chi tiết"};
        createHeaderRow(sheet, styles.header(), headers);
        int rowIndex = 1;
        for (BugExportRowView bug : rows) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;
            setCell(row, col++, bug.getId());
            setCell(row, col++, bug.getTitle());
            setCell(row, col++, bug.getStatus());
            setCell(row, col++, bug.getSeverity());
            setCell(row, col++, bug.getPriority());
            setCell(row, col++, bug.getSprintName());
            setCell(row, col++, bug.getTaskTitle());
            setCell(row, col++, bug.getBacklogItemTitle());
            setCell(row, col++, bug.getAssigneeUsername());
            setCell(row, col++, bug.getReporterUsername());
            setDateCell(row, col++, bug.getDueDate(), styles.date());
            setCell(row, col++, bug.getReopenedCount());
            setDateTimeCell(row, col++, bug.getResolvedAt(), styles.dateTime());
            setDateTimeCell(row, col++, bug.getClosedAt(), styles.dateTime());
            setDateTimeCell(row, col++, bug.getCreatedAt(), styles.dateTime());
            setDateTimeCell(row, col++, bug.getUpdatedAt(), styles.dateTime());
            setCell(row, col, bug.getDescription());
        }
        finishTable(sheet, rows.size(), headers.length);
    }

    private void createBugAssigneeSheet(Workbook workbook, Styles styles, List<BugExportRowView> rows) {
        Sheet sheet = workbook.createSheet("BUG_BY_ASSIGNEE");
        createHeaderRow(sheet, styles.header(), "Người được giao", "Tổng số Bug", "Đang mở", "Đã giải quyết", "Đã đóng");
        Map<String, List<BugExportRowView>> grouped = rows.stream()
                .collect(Collectors.groupingBy(row -> blank(row.getAssigneeUsername()), LinkedHashMap::new, Collectors.toList()));
        int rowIndex = 1;
        for (Map.Entry<String, List<BugExportRowView>> entry : grouped.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            setCell(row, 0, entry.getKey());
            setCell(row, 1, entry.getValue().size());
            setCell(row, 2, entry.getValue().stream().filter(item -> item.getStatus() != BugStatus.RESOLVED
                    && item.getStatus() != BugStatus.VERIFIED
                    && item.getStatus() != BugStatus.CLOSED && item.getStatus() != BugStatus.CANCELLED).count());
            setCell(row, 3, entry.getValue().stream().filter(item -> item.getStatus() == BugStatus.RESOLVED).count());
            setCell(row, 4, entry.getValue().stream().filter(item -> item.getStatus() == BugStatus.CLOSED).count());
        }
        finishTable(sheet, grouped.size(), 5);
    }

    private void createQaMetricsSheet(Workbook workbook, Styles styles, List<BugExportRowView> rows) {
        Sheet sheet = workbook.createSheet("QA_METRICS");
        createHeaderRow(sheet, styles.header(), "Chỉ số QA", "Giá trị");
        int row = 1;
        row = writeMetric(sheet, row, "Tổng số Bug", rows.size());
        row = writeMetric(sheet, row, "Bug đã giải quyết", rows.stream().filter(item -> item.getStatus() == BugStatus.RESOLVED).count());
        row = writeMetric(sheet, row, "Bug đã đóng", rows.stream().filter(item -> item.getStatus() == BugStatus.CLOSED).count());
        row = writeMetric(sheet, row, "Bug nguy cấp (Critical)", rows.stream().filter(item -> item.getSeverity() == BugSeverity.CRITICAL).count());
        row = writeMetric(sheet, row, "Bug trễ hạn (Overdue)", countOverdueBugs(rows));
        writeMetric(sheet, row, "Tổng số lần mở lại", rows.stream().mapToLong(item -> safeLong(item.getReopenedCount())).sum());
        autoSize(sheet, 2);
    }

    private void createCountSheet(Workbook workbook, Styles styles, String sheetName, Map<String, ? extends Number> values) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet, styles.header(), "Phân loại", "Số lượng");
        int rowIndex = 1;
        for (Map.Entry<String, ? extends Number> entry : values.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            setCell(row, 0, entry.getKey());
            setCell(row, 1, entry.getValue());
        }
        finishTable(sheet, values.size(), 2);
    }

    private Project requireProjectViewAccess(UUID projectId) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);
        return project;
    }

    private void validateOptionalSprint(UUID projectId, UUID sprintId) {
        if (sprintId != null && sprintRepository.findByIdAndProjectId(sprintId, projectId).isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_SPRINT_NOT_IN_PROJECT);
        }
    }

    private void validateOptionalProjectMember(UUID projectId, UUID userId) {
        if (userId != null && projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            throw new BusinessException(ErrorCode.BUG_ASSIGNEE_NOT_PROJECT_MEMBER);
        }
    }

    private DateRange resolveDateRange(LocalDate fromDateInput, LocalDate toDateInput) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate fromDate = fromDateInput == null ? today.minusDays(29) : fromDateInput;
        LocalDate toDate = toDateInput == null ? today : toDateInput;
        if (fromDate.isAfter(toDate)) {
            throw new BusinessException(ErrorCode.BUG_REPORT_DATE_RANGE_INVALID);
        }
        return new DateRange(fromDate, toDate);
    }

    private long countOverdueBugs(List<BugExportRowView> rows) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        return rows.stream()
                .filter(row -> row.getDueDate() != null && row.getDueDate().isBefore(today))
                .filter(row -> row.getStatus() != BugStatus.RESOLVED
                        && row.getStatus() != BugStatus.VERIFIED
                        && row.getStatus() != BugStatus.CLOSED
                        && row.getStatus() != BugStatus.CANCELLED)
                .count();
    }

    private GeneratedExcelFile file(String fileName, byte[] content) {
        return new GeneratedExcelFile(fileName, CONTENT_TYPE, content);
    }

    private String nowFilePart() {
        return LocalDateTime.now(BUSINESS_ZONE).format(FILE_DATE_FORMATTER);
    }

    private String label(Object value) {
        return value == null ? "Chưa xác định" : String.valueOf(value);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? "Chưa gán" : value;
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle, String... headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private int writeKeyValue(Sheet sheet, int rowIndex, CellStyle headerStyle, String key, String value) {
        Row row = sheet.createRow(rowIndex);
        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(value == null ? "" : value);
        return rowIndex + 1;
    }

    private int writeMetric(Sheet sheet, int rowIndex, String key, long value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return rowIndex + 1;
    }

    private void setCell(Row row, int index, Object value) {
        Cell cell = row.createCell(index);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private void setDateCell(Row row, int index, LocalDate value, CellStyle dateStyle) {
        Cell cell = row.createCell(index);
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        cell.setCellValue(value);
        cell.setCellStyle(dateStyle);
    }

    private void setDateTimeCell(Row row, int index, Instant value, CellStyle dateTimeStyle) {
        Cell cell = row.createCell(index);
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        cell.setCellValue(LocalDateTime.ofInstant(value, BUSINESS_ZONE));
        cell.setCellStyle(dateTimeStyle);
    }

    private Styles createStyles(Workbook workbook) {
        CellStyle header = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        header.setFont(font);

        CreationHelper helper = workbook.getCreationHelper();
        CellStyle date = workbook.createCellStyle();
        date.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));
        CellStyle dateTime = workbook.createCellStyle();
        dateTime.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return new Styles(header, date, dateTime);
    }

    private void finishTable(Sheet sheet, int rows, int cols) {
        sheet.createFreezePane(0, 1);
        if (cols > 0) {
            sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rows), 0, cols - 1));
        }
        autoSize(sheet, cols);
    }

    private void autoSize(Sheet sheet, int cols) {
        Row headerRow = sheet.getRow(0);
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            String headerTitle = (headerRow != null && headerRow.getCell(i) != null)
                    ? headerRow.getCell(i).getStringCellValue()
                    : "";

            int minWidth = 4800; // ~18 chars
            if (headerTitle.contains("Mã") || headerTitle.contains("ID") || headerTitle.contains("Tiêu đề")
                    || headerTitle.contains("Mô tả") || headerTitle.contains("Tên")) {
                minWidth = 9000; // ~35 chars for IDs, titles, descriptions
            }

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else {
                sheet.setColumnWidth(i, Math.min(currentWidth + 1400, 24000));
            }
        }
    }

    private record DateRange(LocalDate fromDate, LocalDate toDate) {
    }

    private record Styles(CellStyle header, CellStyle date, CellStyle dateTime) {
    }
}
