package com.project.taskmanagement.enums;

/**
 * Trạng thái Task trên bảng Kanban.
 */
public enum TaskStatus {

    /**
     * Công việc chưa bắt đầu.
     */
    TODO,

    /**
     * Công việc đang được thực hiện.
     */
    IN_PROGRESS,

    /**
     * Công việc đang chờ kiểm tra hoặc review.
     */
    IN_REVIEW,

    /**
     * Công việc đã hoàn thành.
     */
    DONE,

    /**
     * Công việc đang bị chặn.
     */
    BLOCKED,

    /**
     * Công việc đã bị hủy.
     */
    CANCELLED
}