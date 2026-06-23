package com.project.taskmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.core.ErrorResponseSever;
import com.project.taskmanagement.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        ErrorCode errorCode =
                ErrorCode.ACCESS_DENIED;

        response.setStatus(
                errorCode.status().value()
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ApiResponseSever<Void> responseBody =
                ApiResponseSever.error(
                        new ErrorResponseSever(
                                errorCode.code(),
                                errorCode.message()
                        )
                );

        objectMapper.writeValue(
                response.getWriter(),
                responseBody
        );
    }
}