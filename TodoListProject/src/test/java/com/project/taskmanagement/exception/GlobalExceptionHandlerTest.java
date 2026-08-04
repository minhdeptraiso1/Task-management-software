package com.project.taskmanagement.exception;

import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.core.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessException_shouldUseDynamicMessageAndRequestPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/missing");

        ResponseEntity<ApiResponseSever<Void>> result = handler.handleBusiness(
                new BusinessException(ErrorCode.PROJECT_NOT_FOUND, "Không tìm thấy Project"),
                request
        );

        assertThat(result.getStatusCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND.status());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND.code());
        assertThat(result.getBody().message()).isEqualTo("Không tìm thấy Project");
        assertThat(result.getBody().path()).isEqualTo("/projects/missing");
    }

    @Test
    void bindException_shouldReturnAllFieldErrorsInStableOrder() {
        BindException exception = new BindException(new Object(), "request");
        exception.getBindingResult().addError(new FieldError("request", "name", "Tên không được để trống"));
        exception.getBindingResult().addError(new FieldError("request", "code", "Mã không được để trống"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects");

        ResponseEntity<ApiResponseSever<ValidationErrorResponse>> result =
                handler.handleBindException(exception, request);

        assertThat(result.getStatusCode()).isEqualTo(ErrorCode.VALIDATION_FAILED.status());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo(422000);
        assertThat(result.getBody().path()).isEqualTo("/projects");
        assertThat(result.getBody().data().errors())
                .extracting("field")
                .containsExactly("code", "name");
    }
}
