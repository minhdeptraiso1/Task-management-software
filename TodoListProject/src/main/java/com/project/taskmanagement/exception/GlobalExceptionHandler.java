package com.project.taskmanagement.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.core.FieldErrorResponse;
import com.project.taskmanagement.dto.response.core.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleBusiness(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.UNAUTHENTICATED, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.ACCESS_DENIED, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseSever<ValidationErrorResponse>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseSever<ValidationErrorResponse>> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseSever<ValidationErrorResponse>> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .sorted(Comparator.comparing(FieldErrorResponse::field))
                .toList();

        return buildValidationResponse(errors, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMissingRequestParam(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ErrorCode.INVALID_PARAMETER,
                "Thiếu tham số bắt buộc: " + exception.getParameterName(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;
        Class<?> requiredType = exception.getRequiredType();

        if (requiredType != null && requiredType.isEnum()) {
            errorCode = ErrorCode.INVALID_ENUM_VALUE;
        } else if (requiredType != null && "UUID".equals(requiredType.getSimpleName())) {
            errorCode = ErrorCode.INVALID_UUID_FORMAT;
        }

        String typeName = requiredType == null ? "không xác định" : requiredType.getSimpleName();
        String message = "Tham số '" + exception.getName()
                + "' không hợp lệ. Kiểu dữ liệu yêu cầu: " + typeName;

        return buildErrorResponse(errorCode, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleJsonParse(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;
        String message = errorCode.message();
        Throwable cause = exception.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "không xác định"
                    : invalidFormatException.getPath().getFirst().getFieldName();
            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType != null && targetType.isEnum()) {
                errorCode = ErrorCode.INVALID_ENUM_VALUE;
                message = "Giá trị không hợp lệ cho trường '" + fieldName
                        + "'. Các giá trị hợp lệ: " + Arrays.toString(targetType.getEnumConstants());
            } else {
                message = "Giá trị không hợp lệ cho trường '" + fieldName + "'";
            }
        }

        return buildErrorResponse(errorCode, message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.TASK_CONCURRENT_MODIFICATION, request);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponseSever<Void>> handlePessimisticLockingFailure(
            PessimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.KANBAN_POSITION_CONFLICT, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.error("Data integrity error at path {}", request.getRequestURI(), exception);

        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        String rootMessage = exception.getMostSpecificCause() == null
                ? ""
                : exception.getMostSpecificCause().getMessage();
        String normalizedMessage = rootMessage == null ? "" : rootMessage.toLowerCase();

        if (normalizedMessage.contains("duplicate") || normalizedMessage.contains("unique")) {
            errorCode = ErrorCode.DATA_ALREADY_EXISTS;
        } else if (normalizedMessage.contains("foreign key")) {
            errorCode = ErrorCode.FOREIGN_KEY_VIOLATION;
        } else if (normalizedMessage.contains("not-null") || normalizedMessage.contains("null value")) {
            errorCode = ErrorCode.MISSING_REQUIRED_FIELD;
        }

        return buildErrorResponse(errorCode, request);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleCannotCreateTransaction(
            CannotCreateTransactionException exception,
            HttpServletRequest request
    ) {
        log.error("Cannot create transaction at path {}", request.getRequestURI(), exception);
        return buildErrorResponse(ErrorCode.DATABASE_ERROR, request);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTransactionSystem(
            TransactionSystemException exception,
            HttpServletRequest request
    ) {
        log.error("Transaction error at path {}", request.getRequestURI(), exception);
        return buildErrorResponse(ErrorCode.TRANSACTION_ERROR, request);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleLazyInitialization(
            LazyInitializationException exception,
            HttpServletRequest request
    ) {
        log.error("Lazy loading error at path {}", request.getRequestURI(), exception);
        return buildErrorResponse(ErrorCode.LAZY_LOADING_ERROR, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataAccess(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        log.error("Database error at path {}", request.getRequestURI(), exception);
        return buildErrorResponse(ErrorCode.DATABASE_ERROR, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ErrorCode.FILE_TOO_LARGE, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseSever<Void>> handleSystem(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled system exception at path {}", request.getRequestURI(), exception);
        return buildErrorResponse(ErrorCode.SYSTEM_ERROR, request);
    }

    private ResponseEntity<ApiResponseSever<ValidationErrorResponse>> buildValidationResponse(
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = bindingResult.getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildValidationResponse(errors, request);
    }

    private ResponseEntity<ApiResponseSever<ValidationErrorResponse>> buildValidationResponse(
            List<FieldErrorResponse> errors,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        ApiResponseSever<ValidationErrorResponse> response = ApiResponseSever
                .of(errorCode.code(), errorCode.message(), new ValidationErrorResponse(errors))
                .withPath(request.getRequestURI());

        return ResponseEntity.status(errorCode.status()).body(response);
    }

    private ResponseEntity<ApiResponseSever<Void>> buildErrorResponse(
            ErrorCode errorCode,
            HttpServletRequest request
    ) {
        return buildErrorResponse(errorCode, errorCode.message(), request);
    }

    private ResponseEntity<ApiResponseSever<Void>> buildErrorResponse(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        ApiResponseSever<Void> response = ApiResponseSever
                .<Void>of(errorCode.code(), message, null)
                .withPath(request.getRequestURI());

        return ResponseEntity.status(errorCode.status()).body(response);
    }
}
