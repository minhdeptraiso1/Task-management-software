package com.project.taskmanagement.excel;

public final class TaskExcelColumns {

    private TaskExcelColumns() {
    }

    public static final int BACKLOG_ITEM_TITLE = 0;
    public static final int TASK_TITLE = 1;
    public static final int DESCRIPTION = 2;
    public static final int TYPE = 3;
    public static final int PRIORITY = 4;
    public static final int ESTIMATED_MINUTES = 5;
    public static final int ASSIGNEE_EMAIL = 6;

    public static final int COLUMN_COUNT = 7;

    public static final int HEADER_ROW_INDEX = 0;
    public static final int DATA_START_ROW_INDEX = 1;

    /**
     * Excel sử dụng index dòng bắt đầu từ 0.
     * Cho phép người dùng nhập tối đa khoảng 1000 dòng.
     */
    public static final int MAX_IMPORT_ROW_INDEX = 1000;
}
