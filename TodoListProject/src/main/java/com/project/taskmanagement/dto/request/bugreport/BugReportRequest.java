package com.project.taskmanagement.dto.request.bugreport;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;

import java.time.LocalDate;
import java.util.UUID;

public record BugReportRequest(
        LocalDate fromDate,
        LocalDate toDate,
        UUID sprintId,
        UUID assigneeUserId,
        BugStatus status,
        BugSeverity severity,
        TaskPriority priority,
        Boolean overdueOnly
) {
}
