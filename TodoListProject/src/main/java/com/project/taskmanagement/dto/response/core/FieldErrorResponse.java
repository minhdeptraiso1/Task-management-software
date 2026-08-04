package com.project.taskmanagement.dto.response.core;

public record FieldErrorResponse(
        String field,
        String message
) {
}
