package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.taskimport.TaskImportErrorResponse;
import com.project.taskmanagement.dto.response.taskimport.TaskImportResponse;
import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.excel.TaskExcelColumns;
import com.project.taskmanagement.excel.TaskExcelHeaders;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.TaskExcelImportService;
import com.project.taskmanagement.service.TaskImportBatchStateService;
import com.project.taskmanagement.service.TaskImportWriterService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import com.project.taskmanagement.service.model.TaskImportRowData;
import com.project.taskmanagement.service.model.TaskImportValidationContext;
import com.project.taskmanagement.service.model.TaskImportValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskExcelImportServiceImpl
        implements TaskExcelImportService {

    static long MAX_FILE_SIZE =
            5L * 1024L * 1024L;

    static int MAX_DATA_ROWS = 1000;

    static String TASK_IMPORT_SHEET =
            "TASK_IMPORT";

    BacklogItemRepository backlogItemRepository;
    SprintRepository sprintRepository;
    ProjectMemberRepository projectMemberRepository;
    UserLookupHelper userLookupHelper;

    TaskImportBatchRepository taskImportBatchRepository;
    TaskImportErrorRepository taskImportErrorRepository;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    TaskImportWriterService taskImportWriterService;
    TaskImportBatchStateService taskImportBatchStateService;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    @CacheEvict(
            value = CacheNames.ADMIN_IMPORT_AUDIT_SEARCH,
            allEntries = true
    )
    public TaskImportResponse importTasks(
            UUID projectId,
            UUID sprintId,
            MultipartFile file
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

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

        validateSprintForImport(sprint);
        validateFile(file);

        TaskImportBatch batch =
                createBatch(
                        projectId,
                        sprintId,
                        currentUser.getId(),
                        file.getOriginalFilename()
                );
        long startMillis = System.currentTimeMillis();

        systemAuditService.log(
                new SystemAuditCommand(
                        currentUser.getId(),
                        SystemAuditAction.IMPORT_EXECUTED,
                        SystemAuditResourceType.TASK_IMPORT_BATCH,
                        batch.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        importAuditValue(batch),
                        true,
                        null
                )
        );

        try (
                InputStream inputStream =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(
                                inputStream
                        )
        ) {
            Sheet sheet =
                    workbook.getSheet(
                            TASK_IMPORT_SHEET
                    );

            if (sheet == null) {
                taskImportBatchStateService.failed(
                        batch,
                        "Không tìm thấy sheet TASK_IMPORT"
                );

                throw new BusinessException(
                        ErrorCode.TASK_IMPORT_SHEET_NOT_FOUND
                );
            }

            validateHeaders(sheet);

            List<RawTaskImportRow> rawRows =
                    readRows(sheet);

            if (rawRows.isEmpty()) {
                taskImportBatchStateService.failed(
                        batch,
                        "File không có dòng Task cần import"
                );

                throw new BusinessException(
                        ErrorCode.TASK_IMPORT_NO_DATA
                );
            }

            batch.setTotalRows(
                    rawRows.size()
            );

            taskImportBatchRepository.save(batch);

            TaskImportValidationResult result =
                    validateRows(
                            projectId,
                            sprintId,
                            rawRows
                    );

            if (result.hasErrors()) {
                taskImportBatchStateService.validationFailed(
                        batch,
                        result.errors()
                );

                return TaskImportResponse.validationFailed(
                        batch.getId(),
                        rawRows.size(),
                        result.errors()
                );
            }

            List<UUID> taskIds =
                    taskImportWriterService.importAll(
                            projectId,
                            sprintId,
                            currentUser.getId(),
                            result.validRows(),
                            batch
                    );

            return TaskImportResponse.completed(
                    batch.getId(),
                    rawRows.size(),
                    taskIds
            );

        } catch (BusinessException exception) {
            if (batch.getStatus() == TaskImportStatus.VALIDATING) {
                taskImportBatchStateService.failed(
                        batch,
                        exception.getMessage()
                );
            }

            throw exception;

        } catch (IOException exception) {
            taskImportBatchStateService.failed(
                    batch,
                    "Không thể đọc file Excel"
            );

            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_PROCESSING_FAILED
            );

        } catch (RuntimeException exception) {
            taskImportBatchStateService.failed(
                    batch,
                    safeMessage(exception)
            );

            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_PROCESSING_FAILED
            );
        } finally {
            log.info(
                    "[TASK-IMPORT] batchId={}, projectId={}, sprintId={}, totalRows={}, successRows={}, failedRows={}, durationMs={}",
                    batch.getId(),
                    projectId,
                    sprintId,
                    batch.getTotalRows(),
                    batch.getSuccessRows(),
                    batch.getFailedRows(),
                    System.currentTimeMillis() - startMillis
            );
        }
    }

    private void validateSprintForImport(
            Sprint sprint
    ) {
        boolean allowed =
                sprint.getStatus()
                        == SprintStatus.PLANNING
                        || sprint.getStatus()
                        == SprintStatus.ACTIVE;

        if (!allowed) {
            throw new BusinessException(
                    ErrorCode.SPRINT_NOT_EDITABLE
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_FILE_REQUIRED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_FILE_TOO_LARGE
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null
                || !fileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".xlsx")) {

            throw new BusinessException(
                    ErrorCode
                            .TASK_IMPORT_FILE_TYPE_INVALID
            );
        }
    }

    private TaskImportBatch createBatch(
            UUID projectId,
            UUID sprintId,
            UUID currentUserId,
            String originalFileName
    ) {
        TaskImportBatch batch =
                TaskImportBatch.builder()
                        .projectId(projectId)
                        .sprintId(sprintId)
                        .importedByUserId(
                                currentUserId
                        )
                        .originalFileName(
                                originalFileName != null
                                        ? originalFileName
                                        : "tasks.xlsx"
                        )
                        .status(
                                TaskImportStatus.VALIDATING
                        )
                        .totalRows(0)
                        .successRows(0)
                        .failedRows(0)
                        .startedAt(
                                Instant.now()
                        )
                        .completedAt(null)
                        .errorMessage(null)
                        .build();

        return taskImportBatchRepository.save(
                batch
        );
    }

    private Map<String, Object> importAuditValue(
            TaskImportBatch batch
    ) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put("batchId", batch.getId());
        value.put("projectId", batch.getProjectId());
        value.put("sprintId", batch.getSprintId());
        value.put("fileName", batch.getOriginalFileName());
        value.put("status", batch.getStatus());
        value.put("totalRows", batch.getTotalRows());
        value.put("successRows", batch.getSuccessRows());
        value.put("failedRows", batch.getFailedRows());
        value.put("importedByUserId", batch.getImportedByUserId());
        value.put("startedAt", batch.getStartedAt());
        value.put("completedAt", batch.getCompletedAt());
        value.put("errorMessage", batch.getErrorMessage());

        return value;
    }

    private void validateHeaders(
            Sheet sheet
    ) {
        Row headerRow =
                sheet.getRow(
                        TaskExcelColumns.HEADER_ROW_INDEX
                );

        if (headerRow == null) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_HEADER_INVALID
            );
        }

        DataFormatter formatter =
                new DataFormatter();

        for (int index = 0;
             index < TaskExcelHeaders.VALUES.size();
             index++) {

            String expected =
                    TaskExcelHeaders.VALUES.get(index);

            String actual =
                    formatter.formatCellValue(
                            headerRow.getCell(index)
                    ).trim();

            if (!expected.equals(actual)) {
                throw new BusinessException(
                        ErrorCode
                                .TASK_IMPORT_HEADER_INVALID
                );
            }
        }
    }

    private List<RawTaskImportRow> readRows(
            Sheet sheet
    ) {
        int lastRowIndex =
                sheet.getLastRowNum();

        int possibleDataRows =
                lastRowIndex
                        - TaskExcelColumns
                        .DATA_START_ROW_INDEX
                        + 1;

        if (possibleDataRows > MAX_DATA_ROWS) {
            throw new BusinessException(
                    ErrorCode.TASK_IMPORT_TOO_MANY_ROWS
            );
        }

        List<RawTaskImportRow> result =
                new ArrayList<>();

        DataFormatter formatter =
                new DataFormatter();

        for (int rowIndex =
             TaskExcelColumns.DATA_START_ROW_INDEX;
             rowIndex <= lastRowIndex;
             rowIndex++) {

            Row row =
                    sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            String taskTitle =
                    readText(
                            row,
                            TaskExcelColumns.TASK_TITLE,
                            formatter
                    );

            /*
             * D?ng kh?ng c? TASK_TITLE ???c coi l? ch?a d?ng.
             */
            if (taskTitle == null
                    || taskTitle.isBlank()) {
                continue;
            }

            result.add(
                            new RawTaskImportRow(
                            rowIndex + 1,
                            null,
                            readText(
                                    row,
                                    TaskExcelColumns
                                            .BACKLOG_ITEM_TITLE,
                                    formatter
                            ),
                            taskTitle,
                            readText(
                                    row,
                                    TaskExcelColumns.DESCRIPTION,
                                    formatter
                            ),
                            readText(
                                    row,
                                    TaskExcelColumns.TYPE,
                                    formatter
                            ),
                            readText(
                                    row,
                                    TaskExcelColumns.PRIORITY,
                                    formatter
                            ),
                            readText(
                                    row,
                                    TaskExcelColumns
                                            .ESTIMATED_MINUTES,
                                    formatter
                            ),
                            readText(
                                    row,
                                    TaskExcelColumns
                                            .ASSIGNEE_EMAIL,
                                    formatter
                            )
                    )
            );
        }

        return result;
    }

    private TaskImportValidationResult validateRows(
            UUID projectId,
            UUID sprintId,
            List<RawTaskImportRow> rawRows
    ) {
        TaskImportValidationContext context = loadValidationContext(projectId, sprintId);
        Map<UUID, BacklogItem> backlogById = context.backlogItemsById();

        Map<String, BacklogItem> backlogByTitle =
                loadSprintBacklogItemsByTitle(
                        backlogById.values()
                );

        Map<String, User> memberByEmail = context.usersByEmail();

        List<TaskImportRowData> validRows =
                new ArrayList<>();

        List<TaskImportErrorResponse> errors =
                new ArrayList<>();

        Set<String> duplicateKeys =
                new HashSet<>();

        for (RawTaskImportRow raw : rawRows) {
            int errorsBefore =
                    errors.size();

            BacklogItem backlogItem =
                    resolveBacklogItemByTitle(
                            raw,
                            backlogByTitle,
                            errors
                    );

            UUID backlogItemId = null;

            if (backlogItem != null) {
                backlogItemId =
                        backlogItem.getId();
            }

            String title =
                    normalizeRequiredText(
                            raw.taskTitle(),
                            255,
                            raw.rowNumber(),
                            "TASK_TITLE",
                            errors
                    );

            String description =
                    normalizeOptionalText(
                            raw.description(),
                            10000,
                            raw.rowNumber(),
                            "DESCRIPTION",
                            errors
                    );

            TaskType type =
                    parseEnum(
                            raw.type(),
                            TaskType.class,
                            TaskType.DEVELOPMENT,
                            raw.rowNumber(),
                            "TYPE",
                            errors
                    );

            TaskPriority priority =
                    parseEnum(
                            raw.priority(),
                            TaskPriority.class,
                            TaskPriority.MEDIUM,
                            raw.rowNumber(),
                            "PRIORITY",
                            errors
                    );

            Integer estimatedMinutes =
                    parseNonNegativeInteger(
                            raw.estimatedMinutes(),
                            raw.rowNumber(),
                            "ESTIMATED_MINUTES",
                            errors
                    );

            String assigneeEmail =
                    normalizeEmail(
                            raw.assigneeEmail()
                    );

            if (assigneeEmail != null
                    && !memberByEmail.containsKey(
                    assigneeEmail
            )) {
                addError(
                        errors,
                        raw.rowNumber(),
                        "ASSIGNEE_EMAIL",
                        raw.assigneeEmail(),
                        "Email không thuộc thành viên đang hoạt động của Project"
                );
            }

            if (backlogItemId != null
                    && title != null) {

                String duplicateKey =
                        backlogItemId
                                + "|"
                                + title
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

                if (!duplicateKeys.add(
                        duplicateKey
                )) {
                    addError(
                            errors,
                            raw.rowNumber(),
                            "TASK_TITLE",
                            raw.taskTitle(),
                            "Task bị trùng trong cùng file và Backlog Item"
                    );
                }
            }

            if (errors.size() == errorsBefore) {
                validRows.add(
                        new TaskImportRowData(
                                raw.rowNumber(),
                                backlogItemId,
                                backlogItem != null
                                        ? backlogItem.getTitle()
                                        : raw.backlogItemTitle(),
                                title,
                                description,
                                type,
                                priority,
                                assigneeEmail,
                                estimatedMinutes,
                                null,
                                null
                        )
                );
            }
        }

        return new TaskImportValidationResult(
                validRows,
                errors
        );
    }

    private TaskImportValidationContext loadValidationContext(
            UUID projectId,
            UUID sprintId
    ) {
        List<BacklogItem> backlogItems = backlogItemRepository
                .findAllByProjectIdAndSprintIdOrderByPositionAsc(projectId, sprintId);
        Map<UUID, BacklogItem> backlogItemsById = new LinkedHashMap<>();
        backlogItems.forEach(item -> backlogItemsById.put(item.getId(), item));

        List<ProjectMember> memberships = projectMemberRepository
                .findAllByProjectIdOrderByJoinedAtAsc(projectId);
        Map<UUID, ProjectMember> membersByUserId = new LinkedHashMap<>();
        memberships.forEach(member -> membersByUserId.put(member.getUserId(), member));

        Map<UUID, User> usersById = userLookupHelper.findUserMap(membersByUserId.keySet());
        Map<String, User> usersByEmail = new LinkedHashMap<>();
        usersById.values().stream()
                .filter(this::isUserEnabled)
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                .forEach(user -> usersByEmail.put(normalizeEmail(user.getEmail()), user));

        return new TaskImportValidationContext(
                backlogItemsById,
                usersByEmail,
                membersByUserId
        );
    }

    private boolean isUserEnabled(
            User user
    ) {
        return user.isEnabled();
    }

    private Map<String, BacklogItem> loadSprintBacklogItemsByTitle(
            Collection<BacklogItem> items
    ) {
        Map<String, BacklogItem> result =
                new HashMap<>();

        Set<String> duplicatedTitles =
                new HashSet<>();

        for (BacklogItem item : items) {
            String titleKey =
                    normalizeBacklogTitle(
                            item.getTitle()
                    );

            if (titleKey == null) {
                continue;
            }

            if (result.containsKey(titleKey)) {
                duplicatedTitles.add(titleKey);
                continue;
            }

            result.put(
                    titleKey,
                    item
            );
        }

        duplicatedTitles.forEach(
                result::remove
        );

        return result;
    }

    private BacklogItem resolveBacklogItemByTitle(
            RawTaskImportRow raw,
            Map<String, BacklogItem> backlogByTitle,
            List<TaskImportErrorResponse> errors
    ) {
        String titleKey =
                normalizeBacklogTitle(
                        raw.backlogItemTitle()
                );

        if (titleKey == null) {
            addError(
                    errors,
                    raw.rowNumber(),
                    "BACKLOG_ITEM_TITLE",
                    raw.backlogItemTitle(),
                    "BACKLOG_ITEM_TITLE không được để trống"
            );

            return null;
        }

        BacklogItem backlogItem =
                backlogByTitle.get(titleKey);

        if (backlogItem == null) {
            addError(
                    errors,
                    raw.rowNumber(),
                    "BACKLOG_ITEM_TITLE",
                    raw.backlogItemTitle(),
                    "BACKLOG_ITEM_TITLE không thuộc Sprint hoặc bị trùng tên"
            );
        }

        return backlogItem;
    }

    private String normalizeBacklogTitle(
            String value
    ) {
        if (value == null
                || value.trim().isBlank()) {
            return null;
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private UUID parseUuidIfPresent(
            String rawValue,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (rawValue == null
                || rawValue.isBlank()) {
            return null;
        }

        return parseUuid(
                rawValue,
                rowNumber,
                fieldName,
                errors
        );
    }

    private UUID parseUuid(
            String rawValue,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (rawValue == null
                || rawValue.isBlank()) {

            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    rawValue,
                    fieldName
                            + " không được để trống"
            );

            return null;
        }

        try {
            return UUID.fromString(
                    rawValue.trim()
            );

        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    rawValue,
                    fieldName
                            + " không đúng định dạng UUID"
            );

            return null;
        }
    }

    private String normalizeRequiredText(
            String value,
            int maxLength,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (value == null
                || value.trim().isBlank()) {

            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    value,
                    fieldName
                            + " không được để trống"
            );

            return null;
        }

        String normalized =
                value.trim();

        if (normalized.length() > maxLength) {
            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    value,
                    fieldName
                            + " không được vượt quá "
                            + maxLength
                            + " ký tự"
            );

            return null;
        }

        return normalized;
    }

    private String normalizeOptionalText(
            String value,
            int maxLength,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (value == null
                || value.trim().isBlank()) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.length() > maxLength) {
            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    value,
                    fieldName
                            + " không được vượt quá "
                            + maxLength
                            + " ký tự"
            );

            return null;
        }

        return normalized;
    }

    private <E extends Enum<E>> E parseEnum(
            String rawValue,
            Class<E> enumType,
            E defaultValue,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (rawValue == null
                || rawValue.isBlank()) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(
                    enumType,
                    rawValue
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );

        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    rawValue,
                    fieldName
                            + " không hợp lệ"
            );

            return null;
        }
    }

    private Integer parseNonNegativeInteger(
            String rawValue,
            int rowNumber,
            String fieldName,
            List<TaskImportErrorResponse> errors
    ) {
        if (rawValue == null
                || rawValue.isBlank()) {
            return null;
        }

        try {
            int value =
                    Integer.parseInt(
                            rawValue.trim()
                    );

            if (value < 0) {
                addError(
                        errors,
                        rowNumber,
                        fieldName,
                        rawValue,
                        fieldName
                                + " phải lớn hơn hoặc bằng 0"
                );

                return null;
            }

            return value;

        } catch (NumberFormatException exception) {
            addError(
                    errors,
                    rowNumber,
                    fieldName,
                    rawValue,
                    fieldName
                            + " phải là số phút hợp lệ"
            );

            return null;
        }
    }

    private String readText(
            Row row,
            int columnIndex,
            DataFormatter formatter
    ) {
        Cell cell =
                row.getCell(columnIndex);

        if (cell == null) {
            return null;
        }

        String value =
                formatter
                        .formatCellValue(cell)
                        .trim();

        return value.isBlank()
                ? null
                : value;
    }

    private void addError(
            List<TaskImportErrorResponse> errors,
            int rowNumber,
            String field,
            String rawValue,
            String message
    ) {
        errors.add(
                new TaskImportErrorResponse(
                        rowNumber,
                        field,
                        rawValue,
                        message
                )
        );
    }

    private String normalizeEmail(
            String value
    ) {
        if (value == null
                || value.trim().isBlank()) {
            return null;
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String safeMessage(
            Throwable throwable
    ) {
        if (throwable == null
                || throwable.getMessage() == null
                || throwable.getMessage().isBlank()) {

            return "Không thể xử lý file Excel";
        }

        String message =
                throwable.getMessage();

        return message.length() > 2000
                ? message.substring(0, 2000)
                : message;
    }

    private record RawTaskImportRow(

            int rowNumber,

            String backlogItemId,

            String backlogItemTitle,

            String taskTitle,

            String description,

            String type,

            String priority,

            String estimatedMinutes,

            String assigneeEmail

    ) {
    }
}
