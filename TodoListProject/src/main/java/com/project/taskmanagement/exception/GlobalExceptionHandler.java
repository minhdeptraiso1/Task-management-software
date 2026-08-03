package com.project.taskmanagement.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.core.ErrorResponseSever;
import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===================== BUSINESS =====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleBusiness(
            BusinessException ex
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        return buildErrorResponse(
                errorCode,
                errorCode.message()
        );
    }

    // ===================== SECURITY =====================

    /**
     * Chưa đăng nhập hoặc không có Authentication hợp lệ.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleAuthentication(
            AuthenticationException ex
    ) {
        return buildErrorResponse(
                ErrorCode.UNAUTHENTICATED,
                ErrorCode.UNAUTHENTICATED.message()
        );
    }

    /**
     * Đã đăng nhập nhưng không đủ quyền.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return buildErrorResponse(
                ErrorCode.ACCESS_DENIED,
                ErrorCode.ACCESS_DENIED.message()
        );
    }

    // ===================== VALIDATION =====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .findFirst()
                .orElse(
                        ErrorCode.VALIDATION_ERROR.message()
                );

        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                message
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation ->
                        violation.getPropertyPath()
                                + ": "
                                + violation.getMessage()
                )
                .findFirst()
                .orElse(
                        ErrorCode.VALIDATION_ERROR.message()
                );

        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                message
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleBindException(
            BindException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .findFirst()
                .orElse(
                        ErrorCode.VALIDATION_ERROR.message()
                );

        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                message
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMissingRequestParam(
            MissingServletRequestParameterException ex
    ) {
        String message =
                "Thiếu tham số bắt buộc: "
                        + ex.getParameterName();

        return buildErrorResponse(
                ErrorCode.INVALID_PARAMETER,
                message
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        ErrorCode errorCode =
                ErrorCode.INVALID_PARAMETER;

        if (ex.getRequiredType() != null
                && ex.getRequiredType().isEnum()) {
            errorCode =
                    ErrorCode.INVALID_ENUM_VALUE;
        } else if (ex.getRequiredType() != null
                && "UUID".equals(ex.getRequiredType().getSimpleName())) {
            errorCode =
                    ErrorCode.INVALID_UUID_FORMAT;
        }

        String requiredType =
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "không xác định";

        String message =
                "Tham số '"
                        + ex.getName()
                        + "' không hợp lệ. Kiểu dữ liệu yêu cầu: "
                        + requiredType;

        return buildErrorResponse(
                errorCode,
                message
        );
    }

    // ===================== REQUEST BODY =====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleJsonParse(
            HttpMessageNotReadableException ex
    ) {
        String message =
                ErrorCode.INVALID_REQUEST_BODY.message();

        ErrorCode errorCode =
                ErrorCode.INVALID_REQUEST_BODY;

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            String fieldName =
                    invalidFormatException.getPath() != null
                            && !invalidFormatException.getPath().isEmpty()
                            ? invalidFormatException
                              .getPath()
                              .getFirst()
                              .getFieldName()
                            : "không xác định";

            Class<?> targetType =
                    invalidFormatException.getTargetType();

            if (targetType != null
                    && targetType.isEnum()) {

                errorCode = ErrorCode.INVALID_ENUM_VALUE;

                message =
                        "Giá trị không hợp lệ cho trường '"
                                + fieldName
                                + "'. Các giá trị hợp lệ: "
                                + Arrays.toString(
                                targetType.getEnumConstants()
                        );

            } else {
                message =
                        "Giá trị không hợp lệ cho trường '"
                                + fieldName
                                + "'";
            }
        }

        return buildErrorResponse(
                errorCode,
                message
        );
    }

    // ===================== DATABASE =====================

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex
    ) {
        return buildErrorResponse(
                ErrorCode.TASK_CONCURRENT_MODIFICATION,
                ErrorCode.TASK_CONCURRENT_MODIFICATION.message()
        );
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponseSever<Void>> handlePessimisticLockingFailure(
            PessimisticLockingFailureException ex
    ) {
        return buildErrorResponse(
                ErrorCode.KANBAN_POSITION_CONFLICT,
                ErrorCode.KANBAN_POSITION_CONFLICT.message()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        ErrorCode errorCode =
                ErrorCode.DATA_INTEGRITY_VIOLATION;

        String message =
                errorCode.message();

        String rootMessage =
                ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : "";

        if (rootMessage != null) {
            String normalizedMessage =
                    rootMessage.toLowerCase();

            if (normalizedMessage.contains("duplicate")
                    || normalizedMessage.contains("unique")) {

                errorCode =
                        ErrorCode.DATA_ALREADY_EXISTS;

                message =
                        ErrorCode.DATA_ALREADY_EXISTS.message();

            } else if (normalizedMessage.contains("foreign key")) {

                errorCode =
                        ErrorCode.FOREIGN_KEY_VIOLATION;

                message =
                        ErrorCode.FOREIGN_KEY_VIOLATION.message();

            } else if (normalizedMessage.contains("not-null")
                    || normalizedMessage.contains("null value")) {

                errorCode =
                        ErrorCode.MISSING_REQUIRED_FIELD;

                message =
                        ErrorCode.MISSING_REQUIRED_FIELD.message();
            }
        }

        return buildErrorResponse(
                errorCode,
                message
        );
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleCannotCreateTransaction(
            CannotCreateTransactionException ex
    ) {
        return buildErrorResponse(
                ErrorCode.DATABASE_ERROR,
                ErrorCode.DATABASE_ERROR.message()
        );
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleTransactionSystem(
            TransactionSystemException ex
    ) {
        return buildErrorResponse(
                ErrorCode.TRANSACTION_ERROR,
                ErrorCode.TRANSACTION_ERROR.message()
        );
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleLazyInitialization(
            LazyInitializationException ex
    ) {
        return buildErrorResponse(
                ErrorCode.LAZY_LOADING_ERROR,
                ErrorCode.LAZY_LOADING_ERROR.message()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataAccess(
            DataAccessException ex
    ) {
        return buildErrorResponse(
                ErrorCode.DATABASE_ERROR,
                ErrorCode.DATABASE_ERROR.message()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex
    ) {
        return buildErrorResponse(
                ErrorCode.FILE_TOO_LARGE,
                ErrorCode.FILE_TOO_LARGE.message()
        );
    }

    // ===================== FALLBACK =====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseSever<Void>> handleSystem(
            Exception ex
    ) {
        /*
         * Nên thay bằng logger.error(...) trong bước clean code.
         * Tạm giữ printStackTrace để điều tra lỗi khi phát triển.
         */
        ex.printStackTrace();

        return buildErrorResponse(
                ErrorCode.SYSTEM_ERROR,
                ErrorCode.SYSTEM_ERROR.message()
        );
    }

    // ===================== RESPONSE BUILDER =====================

    private ResponseEntity<ApiResponseSever<Void>> buildErrorResponse(
            ErrorCode errorCode,
            String message
    ) {
        ApiResponseSever<Void> response =
                new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(
                                errorCode.code(),
                                message
                        )
                );

        return ResponseEntity
                .status(errorCode.status())
                .body(response);
    }
}

