package com.project.taskmanagement.enums;

/**
 * Trạng thái của dự án.
 */
public enum ProjectStatus {

    /**
     * Dự án đang trong giai đoạn chuẩn bị.
     */
    PLANNING,

    /**
     * Dự án đang được triển khai.
     */
    ACTIVE,

    /**
     * Dự án đang tạm dừng.
     */
    ON_HOLD,

    /**
     * Dự án đã hoàn thành.
     */
    COMPLETED,

    /**
     * Dự án đã bị hủy.
     */
    CANCELLED,

    /**
     * Dự án đã được lưu trữ và chỉ cho phép xem.
     */
    ARCHIVED
}