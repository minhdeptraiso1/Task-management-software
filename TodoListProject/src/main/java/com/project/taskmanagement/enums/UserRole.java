package com.project.taskmanagement.enums;

public enum UserRole {

    /**
     * Quản trị toàn bộ hệ thống.
     */
    ADMIN,

    /**
     * Quản lý dự án, thành viên và tiến độ dự án.
     */
    PROJECT_MANAGER,

    /**
     * Điều phối quy trình Scrum và quản lý Sprint.
     */
    SCRUM_MASTER,

    /**
     * Quản lý Product Backlog và ưu tiên yêu cầu.
     */
    PRODUCT_OWNER,

    /**
     * Thực hiện công việc phát triển trong dự án.
     */
    DEVELOPER,

    /**
     * Kiểm thử và xác nhận chất lượng công việc.
     */
    TESTER,

    /**
     * Chỉ được xem dữ liệu được cấp quyền.
     */
    VIEWER
}