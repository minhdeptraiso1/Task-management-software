package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.imports.TaskImportHistorySearchRequest;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchDetailResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportBatchPageResponse;
import com.project.taskmanagement.dto.response.imports.TaskImportErrorPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskImportHistoryService {

    TaskImportBatchPageResponse getImportHistory(
            UUID projectId,
            TaskImportHistorySearchRequest request,
            Pageable pageable
    );

    TaskImportBatchDetailResponse getImportBatchDetail(
            UUID projectId,
            UUID batchId
    );

    TaskImportErrorPageResponse getImportErrors(
            UUID projectId,
            UUID batchId,
            Pageable pageable
    );
}
