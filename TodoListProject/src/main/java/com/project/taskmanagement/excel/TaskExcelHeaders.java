package com.project.taskmanagement.excel;

import java.util.List;

public final class TaskExcelHeaders {

    private TaskExcelHeaders() {
    }

    public static final List<String> VALUES = List.of(
            "User Story *",
            "Tên task *",
            "Mô tả",
            "Loại task *",
            "Độ ưu tiên *",
            "Thời gian ước tính (phút)",
            "Email người phụ trách"
    );
}
