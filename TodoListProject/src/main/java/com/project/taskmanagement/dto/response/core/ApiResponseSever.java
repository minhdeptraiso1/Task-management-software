package com.project.taskmanagement.dto.response.core;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseSever<T>(
        int code,
        String message,
        T data,
        Instant timestamp,
        String path
) {

    private static final int SUCCESS_CODE = 1000;
    private static final String SUCCESS_MESSAGE = "Thành công";

    public static <T> ApiResponseSever<T> ok(T data) {
        return new ApiResponseSever<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, Instant.now(), null);
    }

    public static ApiResponseSever<Void> ok() {
        return new ApiResponseSever<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, Instant.now(), null);
    }

    public static <T> ApiResponseSever<T> of(int code, String message, T data) {
        return new ApiResponseSever<>(code, message, data, Instant.now(), null);
    }

    public ApiResponseSever<T> withPath(String path) {
        return new ApiResponseSever<>(code, message, data, timestamp, path);
    }
}
