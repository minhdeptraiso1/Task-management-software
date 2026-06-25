package com.project.taskmanagement.enums;

/**
 * Vai trò của một thành viên trong từng dự án.
 *
 * Một người dùng có thể có vai trò khác nhau
 * trong các dự án khác nhau.
 */
public enum ProjectMemberRole {

    /**
     * Chủ sở hữu dự án.
     * Chỉ tài khoản hệ thống MANAGER được nhận role này.
     */
    OWNER,

    /**
     * Quản lý dự án.
     * Chỉ tài khoản hệ thống MANAGER được nhận role này.
     */
    PROJECT_MANAGER,

    /**
     * Điều phối quy trình Scrum và Sprint.
     */
    SCRUM_MASTER,

    /**
     * Quản lý Product Backlog và mức độ ưu tiên.
     */
    PRODUCT_OWNER,

    /**
     * Thực hiện công việc phát triển.
     */
    DEVELOPER,

    /**
     * Kiểm thử task và tạo bug.
     */
    TESTER,

    /**
     * Chỉ được xem dữ liệu dự án.
     */
    VIEWER
}