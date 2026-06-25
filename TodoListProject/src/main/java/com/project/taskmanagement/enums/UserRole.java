package com.project.taskmanagement.enums;

/**
 * Vai trò cấp toàn hệ thống.
 *
 * Quyền thao tác trong từng dự án không dùng enum này,
 * mà được xác định bởi ProjectMemberRole.
 */
public enum UserRole {

    /**
     * Quản trị toàn bộ hệ thống:
     * tài khoản, audit log, thống kê và cấu hình.
     *
     * ADMIN không tạo hoặc điều hành dự án
     * trong flow nghiệp vụ thông thường.
     */
    ADMIN,

    /**
     * Tài khoản quản lý:
     * được tạo dự án và quản lý các dự án
     * thuộc phạm vi của mình.
     */
    MANAGER,

    /**
     * Nhân viên:
     * chỉ tham gia các dự án được thêm vào
     * và thao tác theo ProjectMemberRole.
     */
    EMPLOYEE
}