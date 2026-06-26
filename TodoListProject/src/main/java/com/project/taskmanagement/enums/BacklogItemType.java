package com.project.taskmanagement.enums;

/**
 * Loại yêu cầu trong Product Backlog.
 */
public enum BacklogItemType {

    /**
     * Nhóm yêu cầu lớn, có thể chứa nhiều User Story.
     */
    EPIC,

    /**
     * Yêu cầu nghiệp vụ theo góc nhìn người dùng.
     */
    USER_STORY,

    /**
     * Chức năng hoặc cải tiến sản phẩm.
     */
    FEATURE,

    /**
     * Công việc kỹ thuật.
     */
    TECHNICAL
}