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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        ErrorCode errorCode =
                ErrorCode.UNAUTHENTICATED;

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