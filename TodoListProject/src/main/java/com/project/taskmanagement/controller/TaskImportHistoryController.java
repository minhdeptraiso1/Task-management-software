package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.imports.TaskImportHistorySearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchDetailResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchPageResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportErrorPageResponse;
import com.project.taskmanagement.service.TaskImportHistoryService;
import com.project.taskmanagement.service.validation.DateRangeValidator;
import com.project.taskmanagement.service.validation.PageableValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/task-imports")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskImportHistoryController {

    private static final Set<String> IMPORT_HISTORY_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "status", "totalRows", "successRows", "failedRows");

    private static final Set<String> IMPORT_ERROR_SORT_FIELDS =
            Set.of("rowNumber", "fieldName", "createdAt");

    TaskImportHistoryService taskImportHistoryService;

    @Operation(summary = "Lấy lịch sử import Task của Project")
    @GetMapping
    public ApiResponseSever<TaskImportBatchPageResponse> getImportHistory(
            @PathVariable
            UUID projectId,

            @Valid
            @ParameterObject
            TaskImportHistorySearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        DateRangeValidator.validate(request == null ? null : request.fromDate(), request == null ? null : request.toDate());
        PageableValidator.validate(pageable, IMPORT_HISTORY_SORT_FIELDS);

        return ApiResponseSever.ok(
                taskImportHistoryService.getImportHistory(
                        projectId,
                        request,
                        pageable
                )
        );
    }

    @Operation(summary = "Lấy chi tiết batch import Task")
    @GetMapping("/{batchId}")
    public ApiResponseSever<TaskImportBatchDetailResponse> getImportBatchDetail(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID batchId
    ) {
        return ApiResponseSever.ok(
                taskImportHistoryService.getImportBatchDetail(
                        projectId,
                        batchId
                )
        );
    }

    @Operation(summary = "Lấy danh sách lỗi của batch import Task")
    @GetMapping("/{batchId}/errors")
    public ApiResponseSever<TaskImportErrorPageResponse> getImportErrors(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID batchId,

            @PageableDefault(size = 50)
            @ParameterObject
            Pageable pageable
    ) {
        PageableValidator.validate(pageable, IMPORT_ERROR_SORT_FIELDS);

        return ApiResponseSever.ok(
                taskImportHistoryService.getImportErrors(
                        projectId,
                        batchId,
                        pageable
                )
        );
    }
}
