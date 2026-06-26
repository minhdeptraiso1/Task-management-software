package com.project.taskmanagement.enums;

/**
 * Trạng thái của Sprint.
 */
public enum SprintStatus {

    /**
     * Sprint đang được chuẩn bị.
     * Có thể thêm hoặc xóa Backlog Item.
     */
    PLANNING,

    /**
     * Sprint đang được thực hiện.
     */
    ACTIVE,

    /**
     * Sprint đã hoàn thành.
     */
    COMPLETED,

    /**
     * Sprint đã bị hủy.
     */
    CANCELLED
}