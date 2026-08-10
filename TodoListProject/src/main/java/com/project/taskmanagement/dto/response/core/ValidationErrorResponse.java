package com.project.taskmanagement.dto.response.core;

import java.util.List;

public record ValidationErrorResponse(
        List<FieldErrorResponse> errors
) {
}
