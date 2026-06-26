package com.project.taskmanagement.enums;

/**
 * Trạng thái của Backlog Item.
 */
public enum BacklogItemStatus {

    /**
     * Yêu cầu mới được tạo, chưa sẵn sàng thực hiện.
     */
    DRAFT,

    /**
     * Yêu cầu đã rõ ràng và có thể đưa vào Sprint.
     */
    READY,

    /**
     * Yêu cầu đã được đưa vào Sprint.
     */
    IN_SPRINT,

    /**
     * Yêu cầu đã hoàn thành.
     */
    DONE,

    /**
     * Yêu cầu đã bị hủy.
     */
    CANCELLED
}