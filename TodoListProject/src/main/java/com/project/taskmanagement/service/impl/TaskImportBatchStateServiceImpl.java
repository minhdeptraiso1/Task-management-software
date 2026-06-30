package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.taskimport.TaskImportErrorResponse;
import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.entity.TaskImportError;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.repository.TaskImportBatchRepository;
import com.project.taskmanagement.repository.TaskImportErrorRepository;
import com.project.taskmanagement.service.TaskImportBatchStateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void validationFailed(
            TaskImportBatch batch,
            List<TaskImportErrorResponse> errors
    ) {
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
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void failed(
            TaskImportBatch batch,
            String message
    ) {
        if (batch == null) {
            return;
        }

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
    }
}