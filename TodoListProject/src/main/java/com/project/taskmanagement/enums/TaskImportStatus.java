package com.project.taskmanagement.enums;

/**
 * Trạng thái một lần import Task từ Excel.
 */
public enum TaskImportStatus {

    VALIDATING,

    VALIDATION_FAILED,

    IMPORTING,

    COMPLETED,

    FAILED
}