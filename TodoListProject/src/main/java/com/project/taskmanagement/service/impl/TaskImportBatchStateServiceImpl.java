package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.response.taskimport.TaskImportErrorResponse;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.TaskImportError;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskImportErrorRepository;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.TaskImportBatchStateService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskImportBatchStateServiceImpl
        implements TaskImportBatchStateService {

    TaskImportBatchRepository taskImportBatchRepository;
    TaskImportErrorRepository taskImportErrorRepository;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ADMIN_IMPORT_AUDIT_SEARCH, allEntries = true)
    })
    public void validationFailed(
            TaskImportBatch batch,
            List<TaskImportErrorResponse> errors
    ) {
        Map<String, Object> oldValue =
                importAuditValue(batch);

        batch.setStatus(
                TaskImportStatus.VALIDATION_FAILED
        );

        batch.setSuccessRows(0);

        batch.setFailedRows(
                errors.size()
        );

        batch.setCompletedAt(
                Instant.now()
        );

        batch.setErrorMessage(
                "File có "
                        + errors.size()
                        + " lỗi validation"
        );

        taskImportBatchRepository.save(batch);

        List<TaskImportError> entities =
                errors.stream()
                        .map(error ->
                                TaskImportError.builder()
                                        .importBatchId(
                                                batch.getId()
                                        )
                                        .rowNumber(
                                                error.rowNumber()
                                        )
                                        .fieldName(
                                                error.fieldName()
                                        )
                                        .rawValue(
                                                error.rawValue()
                                        )
                                        .errorMessage(
                                                error.message()
                                        )
                                        .build()
                        )
                        .toList();

        taskImportErrorRepository.saveAll(
                entities
        );

        logImportAudit(
                batch,
                SystemAuditAction.IMPORT_VALIDATION_FAILED,
                oldValue,
                importAuditValue(batch),
                true,
                batch.getErrorMessage()
        );
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ADMIN_IMPORT_AUDIT_SEARCH, allEntries = true)
    })
    public void failed(
            TaskImportBatch batch,
            String message
    ) {
        if (batch == null) {
            return;
        }

        Map<String, Object> oldValue =
                importAuditValue(batch);

        batch.setStatus(
                TaskImportStatus.FAILED
        );

        batch.setSuccessRows(0);

        batch.setFailedRows(
                batch.getTotalRows()
        );

        batch.setCompletedAt(
                Instant.now()
        );

        batch.setErrorMessage(
                message != null
                        ? message
                        : "Import Task thất bại"
        );

        taskImportBatchRepository.save(batch);

        logImportAudit(
                batch,
                SystemAuditAction.IMPORT_FAILED,
                oldValue,
                importAuditValue(batch),
                false,
                batch.getErrorMessage()
        );
    }

    private void logImportAudit(
            TaskImportBatch batch,
            SystemAuditAction action,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            boolean success,
            String errorMessage
    ) {
        systemAuditService.log(
                new SystemAuditCommand(
                        batch.getImportedByUserId(),
                        action,
                        SystemAuditResourceType.TASK_IMPORT_BATCH,
                        batch.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        oldValue,
                        newValue,
                        success,
                        errorMessage
                )
        );
    }

    private Map<String, Object> importAuditValue(
            TaskImportBatch batch
    ) {
        if (batch == null) {
            return null;
        }

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
}
