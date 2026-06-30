package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.taskimport.TaskImportErrorResponse;
import com.project.taskmanagement.entity.TaskImportBatch;

import java.util.List;

public interface TaskImportBatchStateService {

    void validationFailed(
            TaskImportBatch batch,
            List<TaskImportErrorResponse> errors
    );

    void failed(
            TaskImportBatch batch,
            String message
    );
}