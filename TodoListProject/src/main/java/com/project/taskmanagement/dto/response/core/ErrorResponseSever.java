package com.project.taskmanagement.dto.response.core;

public record ErrorResponseSever(
        int code,
        String message
) {
}
