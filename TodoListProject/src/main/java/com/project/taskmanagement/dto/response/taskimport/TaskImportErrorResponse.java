package com.project.taskmanagement.dto.response.taskimport;

public record TaskImportErrorResponse(

        int rowNumber,

        String fieldName,

        String rawValue,

        String message

) {
}