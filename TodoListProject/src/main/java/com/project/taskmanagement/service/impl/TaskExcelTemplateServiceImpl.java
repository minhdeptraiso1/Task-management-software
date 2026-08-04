package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;
import com.project.taskmanagement.excel.TaskExcelColumns;
import com.project.taskmanagement.excel.TaskExcelHeaders;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.BacklogItemRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.TaskExcelTemplateService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.GeneratedExcelFile;
import com.project.taskmanagement.service.model.TaskExcelMember;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskExcelTemplateServiceImpl
        implements TaskExcelTemplateService {

    static String TASK_IMPORT_SHEET =
            "TASK_IMPORT";

    static String REFERENCE_DATA_SHEET =
            "REFERENCE_DATA";

    static String INSTRUCTION_SHEET =
            "HUONG_DAN";

    static String MEMBER_EMAIL_RANGE =
            "PROJECT_MEMBER_EMAILS";

    static String BACKLOG_TITLE_RANGE =
            "SPRINT_BACKLOG_TITLES";

    static String TASK_TYPE_RANGE =
            "TASK_TYPES";

    static String TASK_PRIORITY_RANGE =
            "TASK_PRIORITIES";

    static int REFERENCE_BACKLOG_ID_COLUMN = 0;
    static int REFERENCE_BACKLOG_TITLE_COLUMN = 1;

    static int REFERENCE_MEMBER_EMAIL_COLUMN = 3;
    static int REFERENCE_MEMBER_USERNAME_COLUMN = 4;
    static int REFERENCE_MEMBER_ROLE_COLUMN = 5;

    static int REFERENCE_TASK_TYPE_COLUMN = 7;
    static int REFERENCE_PRIORITY_COLUMN = 8;

    static Pattern INVALID_FILE_NAME_CHARACTERS =
            Pattern.compile("[\\\\/:*?\"<>|]");

    BacklogItemRepository backlogItemRepository;
    SprintRepository sprintRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    @Override
    @Transactional(readOnly = true)
    public GeneratedExcelFile generateTemplate(
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
                .requireTaskManagementAccess(
                        projectId,
                        currentUser
                );

        Sprint sprint =
                sprintRepository
                        .findByIdAndProjectId(
                                sprintId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SPRINT_NOT_FOUND
                                )
                        );

        List<BacklogItem> backlogItems =
                backlogItemRepository
                        .findAllByProjectIdAndSprintIdOrderByPositionAsc(
                                projectId,
                                sprintId
                        );

        if (backlogItems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_EXCEL_TEMPLATE_NO_BACKLOG_ITEMS
            );
        }

        List<TaskExcelMember> members =
                getProjectMembers(projectId);

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            ExcelStyles styles =
                    createStyles(workbook);

            createInstructionSheet(
                    workbook,
                    project,
                    sprint,
                    styles
            );

            Sheet referenceSheet =
                    createReferenceDataSheet(
                            workbook,
                            backlogItems,
                            members,
                            styles
                    );

            createNamedRanges(
                    workbook,
                    referenceSheet,
                    backlogItems.size(),
                    members.size()
            );

            createTaskImportSheet(
                    workbook,
                    sprint,
                    backlogItems,
                    members,
                    styles
            );

            int referenceSheetIndex =
                    workbook.getSheetIndex(
                            REFERENCE_DATA_SHEET
                    );

            workbook.setSheetHidden(
                    referenceSheetIndex,
                    true
            );

            workbook.setActiveSheet(
                    workbook.getSheetIndex(
                            TASK_IMPORT_SHEET
                    )
            );

            workbook.write(outputStream);

            return new GeneratedExcelFile(
                    buildFileName(sprint),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );

        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode
                            .TASK_EXCEL_TEMPLATE_GENERATION_FAILED
            );
        }
    }

    private String buildFileName(
            Sprint sprint
    ) {
        String sprintName =
                sanitizeFileName(
                        sprint.getName()
                );

        return "task-import-"
                + sprintName
                + "-"
                + LocalDate.now()
                + ".xlsx";
    }

    private List<TaskExcelMember> getProjectMembers(
            UUID projectId
    ) {
        List<ProjectMember> projectMembers =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        List<TaskExcelMember> result =
                new ArrayList<>();

        for (ProjectMember projectMember :
                projectMembers) {

            User user =
                    userRepository
                            .findById(
                                    projectMember.getUserId()
                            )
                            .orElse(null);

            if (user == null) {
                continue;
            }

            /*
             * Keep only active users for the ASSIGNEE_EMAIL dropdown.
             */
            if (!isUserEnabled(user)) {
                continue;
            }

            if (user.getEmail() == null
                    || user.getEmail().isBlank()) {
                continue;
            }

            result.add(
                    new TaskExcelMember(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            projectMember
                                    .getRole()
                                    .name()
                    )
            );
        }

        return result;
    }

    private boolean isUserEnabled(
            User user
    ) {
        /*
         * Keep the active-user check centralized here.
         */

        return user.isEnabled();
    }

    private void createInstructionSheet(
            Workbook workbook,
            Project project,
            Sprint sprint,
            ExcelStyles styles
    ) {
        Sheet sheet =
                workbook.createSheet(
                        INSTRUCTION_SHEET
                );

        sheet.setColumnWidth(0, 7_000);
        sheet.setColumnWidth(1, 22_000);

        int rowIndex = 0;

        Row titleRow =
                sheet.createRow(rowIndex++);

        Cell titleCell =
                titleRow.createCell(0);

        titleCell.setCellValue(
                "HƯỚNG DẪN NHẬP TASK"
        );

        titleCell.setCellStyle(
                styles.titleStyle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        1
                )
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "Project",
                project.getName(),
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "Sprint",
                sprint.getName(),
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "Trạng thái Sprint",
                sprint.getStatus().name(),
                styles
        );

        rowIndex++;

        addInstructionRow(
                sheet,
                rowIndex++,
                "1",
                "Mở sheet TASK_IMPORT.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "2",
                "Mỗi User Story đã được tạo sẵn một dòng.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "3",
                "Điền Tên task cho những dòng cần tạo Task.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "4",
                "Muốn tạo nhiều Task cho cùng User Story, sao chép nguyên dòng đó.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "5",
                "User Story, Loại task, Độ ưu tiên và Email người phụ trách dùng danh sách thả xuống.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "6",
                "Trạng thái Task khi import luôn là TODO.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex++,
                "7",
                "Chỉ dòng có Tên task mới được import.",
                styles
        );

        addInstructionRow(
                sheet,
                rowIndex,
                "8",
                "Không xóa hoặc đổi tên các cột trong TASK_IMPORT.",
                styles
        );
    }
    private void addInstructionRow(
            Sheet sheet,
            int rowIndex,
            String label,
            String content,
            ExcelStyles styles
    ) {
        Row row =
                sheet.createRow(rowIndex);

        Cell labelCell =
                row.createCell(0);

        labelCell.setCellValue(label);
        labelCell.setCellStyle(
                styles.instructionLabelStyle()
        );

        Cell contentCell =
                row.createCell(1);

        contentCell.setCellValue(
                content != null
                        ? content
                        : ""
        );

        contentCell.setCellStyle(
                styles.instructionContentStyle()
        );

        row.setHeightInPoints(28);
    }

    private Sheet createReferenceDataSheet(
            Workbook workbook,
            List<BacklogItem> backlogItems,
            List<TaskExcelMember> members,
            ExcelStyles styles
    ) {
        Sheet sheet =
                workbook.createSheet(
                        REFERENCE_DATA_SHEET
                );

        createReferenceHeaders(
                sheet,
                styles
        );

        writeBacklogReferenceData(
                sheet,
                backlogItems
        );

        writeMemberReferenceData(
                sheet,
                members
        );

        writeEnumReferenceData(
                sheet
        );

        for (int columnIndex = 0;
             columnIndex <= REFERENCE_PRIORITY_COLUMN;
             columnIndex++) {

            sheet.autoSizeColumn(columnIndex);
        }

        return sheet;
    }

    private void createReferenceHeaders(
            Sheet sheet,
            ExcelStyles styles
    ) {
        Row row =
                sheet.createRow(0);

        createStyledCell(
                row,
                REFERENCE_BACKLOG_ID_COLUMN,
                "BACKLOG_ITEM_ID",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_BACKLOG_TITLE_COLUMN,
                "BACKLOG_ITEM_TITLE",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_MEMBER_EMAIL_COLUMN,
                "MEMBER_EMAIL",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_MEMBER_USERNAME_COLUMN,
                "MEMBER_USERNAME",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_MEMBER_ROLE_COLUMN,
                "PROJECT_ROLE",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_TASK_TYPE_COLUMN,
                "TASK_TYPE",
                styles.headerStyle()
        );

        createStyledCell(
                row,
                REFERENCE_PRIORITY_COLUMN,
                "TASK_PRIORITY",
                styles.headerStyle()
        );
    }

    private void writeBacklogReferenceData(
            Sheet sheet,
            List<BacklogItem> backlogItems
    ) {
        for (int index = 0;
             index < backlogItems.size();
             index++) {

            BacklogItem item =
                    backlogItems.get(index);

            Row row =
                    getOrCreateRow(
                            sheet,
                            index + 1
                    );

            row.createCell(
                    REFERENCE_BACKLOG_ID_COLUMN
            ).setCellValue(
                    item.getId().toString()
            );

            row.createCell(
                    REFERENCE_BACKLOG_TITLE_COLUMN
            ).setCellValue(
                    safeText(item.getTitle())
            );
        }
    }

    private void writeMemberReferenceData(
            Sheet sheet,
            List<TaskExcelMember> members
    ) {
        for (int index = 0;
             index < members.size();
             index++) {

            TaskExcelMember member =
                    members.get(index);

            Row row =
                    getOrCreateRow(
                            sheet,
                            index + 1
                    );

            row.createCell(
                    REFERENCE_MEMBER_EMAIL_COLUMN
            ).setCellValue(
                    safeText(member.email())
            );

            row.createCell(
                    REFERENCE_MEMBER_USERNAME_COLUMN
            ).setCellValue(
                    safeText(member.username())
            );

            row.createCell(
                    REFERENCE_MEMBER_ROLE_COLUMN
            ).setCellValue(
                    safeText(member.projectRole())
            );
        }
    }

    private void writeEnumReferenceData(
            Sheet sheet
    ) {
        TaskType[] types =
                TaskType.values();

        TaskPriority[] priorities =
                TaskPriority.values();

        int maxRows =
                Math.max(
                        types.length,
                        priorities.length
                );

        for (int index = 0;
             index < maxRows;
             index++) {

            Row row =
                    getOrCreateRow(
                            sheet,
                            index + 1
                    );

            if (index < types.length) {
                row.createCell(
                        REFERENCE_TASK_TYPE_COLUMN
                ).setCellValue(
                        types[index].name()
                );
            }

            if (index < priorities.length) {
                row.createCell(
                        REFERENCE_PRIORITY_COLUMN
                ).setCellValue(
                        priorities[index].name()
                );
            }
        }
    }

    private void createNamedRanges(
            Workbook workbook,
            Sheet referenceSheet,
            int backlogItemCount,
            int memberCount
    ) {
        createNamedRange(
                workbook,
                BACKLOG_TITLE_RANGE,
                REFERENCE_DATA_SHEET,
                REFERENCE_BACKLOG_TITLE_COLUMN,
                backlogItemCount
        );

        createNamedRange(
                workbook,
                TASK_TYPE_RANGE,
                REFERENCE_DATA_SHEET,
                REFERENCE_TASK_TYPE_COLUMN,
                TaskType.values().length
        );

        createNamedRange(
                workbook,
                TASK_PRIORITY_RANGE,
                REFERENCE_DATA_SHEET,
                REFERENCE_PRIORITY_COLUMN,
                TaskPriority.values().length
        );

        if (memberCount > 0) {
            createNamedRange(
                    workbook,
                    MEMBER_EMAIL_RANGE,
                    REFERENCE_DATA_SHEET,
                    REFERENCE_MEMBER_EMAIL_COLUMN,
                    memberCount
            );
        }
    }

    private void createNamedRange(
            Workbook workbook,
            String rangeName,
            String sheetName,
            int columnIndex,
            int itemCount
    ) {
        if (itemCount <= 0) {
            return;
        }

        Name name =
                workbook.createName();

        name.setNameName(rangeName);

        String columnLetter =
                toExcelColumnLetter(
                        columnIndex
                );

        name.setRefersToFormula(
                "'"
                        + sheetName
                        + "'!$"
                        + columnLetter
                        + "$2:$"
                        + columnLetter
                        + "$"
                        + (itemCount + 1)
        );
    }

    private void createTaskImportSheet(
            Workbook workbook,
            Sprint sprint,
            List<BacklogItem> backlogItems,
            List<TaskExcelMember> members,
            ExcelStyles styles
    ) {
        Sheet sheet =
                workbook.createSheet(
                        TASK_IMPORT_SHEET
                );

        sheet.createFreezePane(
                2,
                1
        );

        createTaskImportHeader(
                sheet,
                styles
        );

        writeTaskImportSeedRows(
                sheet,
                backlogItems,
                styles
        );

        createTaskImportValidations(
                sheet,
                backlogItems,
                members
        );

        applyTaskImportColumnWidths(
                sheet
        );

        sheet.setAutoFilter(
                new CellRangeAddress(
                        TaskExcelColumns.HEADER_ROW_INDEX,
                        TaskExcelColumns.HEADER_ROW_INDEX,
                        0,
                        TaskExcelColumns.COLUMN_COUNT - 1
                )
        );

        sheet.setZoom(90);

        sheet.getPrintSetup().setLandscape(
                true
        );

        sheet.setRepeatingRows(
                new CellRangeAddress(
                        0,
                        0,
                        -1,
                        -1
                )
        );
    }

    private void createTaskImportHeader(
            Sheet sheet,
            ExcelStyles styles
    ) {
        Row headerRow =
                sheet.createRow(
                        TaskExcelColumns.HEADER_ROW_INDEX
                );

        headerRow.setHeightInPoints(32);

        for (int index = 0;
             index < TaskExcelHeaders.VALUES.size();
             index++) {

            Cell cell =
                    headerRow.createCell(index);

            cell.setCellValue(
                    TaskExcelHeaders.VALUES
                            .get(index)
            );

            cell.setCellStyle(
                    styles.headerStyle()
            );
        }
    }

    private void writeTaskImportSeedRows(
            Sheet sheet,
            List<BacklogItem> backlogItems,
            ExcelStyles styles
    ) {
        for (int index = 0;
             index < backlogItems.size();
             index++) {

            BacklogItem backlogItem =
                    backlogItems.get(index);

            int rowIndex =
                    TaskExcelColumns.DATA_START_ROW_INDEX
                            + index;

            Row row =
                    sheet.createRow(rowIndex);

            row.setHeightInPoints(26);

            for (int columnIndex =
                 TaskExcelColumns.BACKLOG_ITEM_TITLE;
                 columnIndex <
                         TaskExcelColumns.COLUMN_COUNT;
                 columnIndex++) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellStyle(
                        styles.inputCellStyle()
                );
            }

            row.getCell(
                    TaskExcelColumns.BACKLOG_ITEM_TITLE
            ).setCellValue(
                    safeText(backlogItem.getTitle())
            );

            row.getCell(
                    TaskExcelColumns.TYPE
            ).setCellValue(
                    TaskType.DEVELOPMENT.name()
            );

            row.getCell(
                    TaskExcelColumns.PRIORITY
            ).setCellValue(
                    TaskPriority.MEDIUM.name()
            );

            row.getCell(
                    TaskExcelColumns.ESTIMATED_MINUTES
            ).setCellValue(60);
        }

        int styledRowEnd =
                Math.min(
                        TaskExcelColumns.MAX_IMPORT_ROW_INDEX,
                        backlogItems.size() + 20
                );

        for (int rowIndex =
             backlogItems.size() + 1;
             rowIndex <= styledRowEnd;
             rowIndex++) {

            Row row =
                    sheet.createRow(rowIndex);

            for (int columnIndex = 0;
                 columnIndex <
                         TaskExcelColumns.COLUMN_COUNT;
                 columnIndex++) {

                Cell cell =
                        row.createCell(columnIndex);

                cell.setCellStyle(
                        styles.inputCellStyle()
                );
            }
        }
    }

    private void createTaskImportValidations(
            Sheet sheet,
            List<BacklogItem> backlogItems,
            List<TaskExcelMember> members
    ) {
        if (!backlogItems.isEmpty()) {
            addNamedRangeValidation(
                    sheet,
                    TaskExcelColumns.BACKLOG_ITEM_TITLE,
                    BACKLOG_TITLE_RANGE,
                    "User Story không thuộc Sprint. Vui lòng chọn đúng trong danh sách."
            );
        }

        addNamedRangeValidation(
                sheet,
                TaskExcelColumns.TYPE,
                TASK_TYPE_RANGE,
                "Loại task không hợp lệ"
        );

        addNamedRangeValidation(
                sheet,
                TaskExcelColumns.PRIORITY,
                TASK_PRIORITY_RANGE,
                "Độ ưu tiên không hợp lệ"
        );

        addWholeNumberValidation(
                sheet,
                TaskExcelColumns.ESTIMATED_MINUTES,
                "Thời gian ước tính phải là số phút lớn hơn hoặc bằng 0"
        );

        if (!members.isEmpty()) {
            addNamedRangeValidation(
                    sheet,
                    TaskExcelColumns.ASSIGNEE_EMAIL,
                    MEMBER_EMAIL_RANGE,
                    "Email không thuộc thành viên Project"
            );
        }
    }

    private void addNamedRangeValidation(
            Sheet sheet,
            int columnIndex,
            String rangeName,
            String errorMessage
    ) {
        DataValidationHelper helper =
                sheet.getDataValidationHelper();

        DataValidationConstraint constraint =
                helper.createFormulaListConstraint(
                        rangeName
                );

        CellRangeAddressList addressList =
                new CellRangeAddressList(
                        TaskExcelColumns.DATA_START_ROW_INDEX,
                        TaskExcelColumns.MAX_IMPORT_ROW_INDEX,
                        columnIndex,
                        columnIndex
                );

        DataValidation validation =
                helper.createValidation(
                        constraint,
                        addressList
                );

        validation.setShowErrorBox(true);
        validation.setErrorStyle(
                DataValidation.ErrorStyle.STOP
        );
        if (validation instanceof XSSFDataValidation) {
            validation.setSuppressDropDownArrow(true);
        } else {
            validation.setSuppressDropDownArrow(false);
        }

        validation.createErrorBox(
                "Dữ liệu không hợp lệ",
                errorMessage
        );
        validation.setShowPromptBox(false);

        sheet.addValidationData(
                validation
        );
    }

    private void addWholeNumberValidation(
            Sheet sheet,
            int columnIndex,
            String errorMessage
    ) {
        DataValidationHelper helper =
                sheet.getDataValidationHelper();

        DataValidationConstraint constraint =
                helper.createIntegerConstraint(
                        DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                        "0",
                        null
                );

        CellRangeAddressList addressList =
                new CellRangeAddressList(
                        TaskExcelColumns.DATA_START_ROW_INDEX,
                        TaskExcelColumns.MAX_IMPORT_ROW_INDEX,
                        columnIndex,
                        columnIndex
                );

        DataValidation validation =
                helper.createValidation(
                        constraint,
                        addressList
                );

        validation.setShowErrorBox(true);
        validation.setErrorStyle(
                DataValidation.ErrorStyle.STOP
        );
        validation.createErrorBox(
                "Dữ liệu không hợp lệ",
                errorMessage
        );
        validation.setShowPromptBox(false);

        sheet.addValidationData(
                validation
        );
    }

    private void applyTaskImportColumnWidths(
            Sheet sheet
    ) {
        sheet.setColumnWidth(
                TaskExcelColumns.BACKLOG_ITEM_TITLE,
                12_000
        );

        sheet.setColumnWidth(
                TaskExcelColumns.TASK_TITLE,
                12_000
        );

        sheet.setColumnWidth(
                TaskExcelColumns.DESCRIPTION,
                16_000
        );

        sheet.setColumnWidth(
                TaskExcelColumns.TYPE,
                5_000
        );

        sheet.setColumnWidth(
                TaskExcelColumns.PRIORITY,
                4_500
        );

        sheet.setColumnWidth(
                TaskExcelColumns.ESTIMATED_MINUTES,
                6_500
        );

        sheet.setColumnWidth(
                TaskExcelColumns.ASSIGNEE_EMAIL,
                9_000
        );
    }

    private ExcelStyles createStyles(
            Workbook workbook
    ) {
        Font titleFont =
                workbook.createFont();

        titleFont.setBold(true);
        titleFont.setFontHeightInPoints(
                (short) 16
        );

        CellStyle titleStyle =
                workbook.createCellStyle();

        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        titleStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        titleStyle.setFillForegroundColor(
                IndexedColors.LIGHT_BLUE
                        .getIndex()
        );

        titleStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        Font headerFont =
                workbook.createFont();

        headerFont.setBold(true);
        headerFont.setColor(
                IndexedColors.WHITE.getIndex()
        );

        CellStyle headerStyle =
                workbook.createCellStyle();

        headerStyle.setFont(headerFont);

        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE
                        .getIndex()
        );

        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        headerStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        headerStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        headerStyle.setWrapText(true);

        applyThinBorder(headerStyle);

        CellStyle referenceCellStyle =
                workbook.createCellStyle();

        referenceCellStyle.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT
                        .getIndex()
        );

        referenceCellStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        referenceCellStyle.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        referenceCellStyle.setWrapText(true);

        applyThinBorder(referenceCellStyle);

        CellStyle inputCellStyle =
                workbook.createCellStyle();

        inputCellStyle.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        inputCellStyle.setWrapText(true);

        applyThinBorder(inputCellStyle);

        CellStyle instructionLabelStyle =
                workbook.createCellStyle();

        Font instructionLabelFont =
                workbook.createFont();

        instructionLabelFont.setBold(true);

        instructionLabelStyle.setFont(
                instructionLabelFont
        );

        instructionLabelStyle.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        applyThinBorder(
                instructionLabelStyle
        );

        CellStyle instructionContentStyle =
                workbook.createCellStyle();

        instructionContentStyle.setWrapText(
                true
        );

        instructionContentStyle
                .setVerticalAlignment(
                        VerticalAlignment.TOP
                );

        applyThinBorder(
                instructionContentStyle
        );

        return new ExcelStyles(
                titleStyle,
                headerStyle,
                referenceCellStyle,
                inputCellStyle,
                instructionLabelStyle,
                instructionContentStyle
        );
    }

    private void applyThinBorder(
            CellStyle style
    ) {
        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );
    }

    private void createStyledCell(
            Row row,
            int columnIndex,
            String value,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(columnIndex);

        cell.setCellValue(
                safeText(value)
        );

        cell.setCellStyle(style);
    }

    private Row getOrCreateRow(
            Sheet sheet,
            int rowIndex
    ) {
        Row row =
                sheet.getRow(rowIndex);

        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        return row;
    }

    private String safeText(
            String value
    ) {
        return value != null
                ? value
                : "";
    }

    private String sanitizeFileName(
            String value
    ) {
        String safeValue =
                value == null
                        ? "sprint"
                        : value.trim();

        safeValue =
                INVALID_FILE_NAME_CHARACTERS
                        .matcher(safeValue)
                        .replaceAll("-");

        safeValue =
                Normalizer.normalize(
                        safeValue,
                        Normalizer.Form.NFD
                );

        safeValue =
                safeValue.replaceAll(
                        "\\p{M}+",
                        ""
                );

        safeValue =
                safeValue.toLowerCase(
                        Locale.ROOT
                );

        safeValue =
                safeValue.replaceAll(
                        "[^a-z0-9-_ ]",
                        ""
                );

        safeValue =
                safeValue.replaceAll(
                        "\\s+",
                        "-"
                );

        safeValue =
                safeValue.replaceAll(
                        "-+",
                        "-"
                );

        if (safeValue.isBlank()) {
            return "sprint";
        }

        return safeValue;
    }

    private String toExcelColumnLetter(
            int zeroBasedColumnIndex
    ) {
        int columnNumber =
                zeroBasedColumnIndex + 1;

        StringBuilder result =
                new StringBuilder();

        while (columnNumber > 0) {
            int remainder =
                    (columnNumber - 1) % 26;

            result.insert(
                    0,
                    (char) ('A' + remainder)
            );

            columnNumber =
                    (columnNumber - 1) / 26;
        }

        return result.toString();
    }

    private record ExcelStyles(

            CellStyle titleStyle,

            CellStyle headerStyle,

            CellStyle referenceCellStyle,

            CellStyle inputCellStyle,

            CellStyle instructionLabelStyle,

            CellStyle instructionContentStyle

    ) {
    }
}
