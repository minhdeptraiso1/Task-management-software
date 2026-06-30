package com.project.taskmanagement.service.model;

import com.project.taskmanagement.dto.response.taskimport.TaskImportErrorResponse;

import java.util.List;

public record TaskImportValidationResult(

        List<TaskImportRowData> validRows,

        List<TaskImportErrorResponse> errors

) {

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}