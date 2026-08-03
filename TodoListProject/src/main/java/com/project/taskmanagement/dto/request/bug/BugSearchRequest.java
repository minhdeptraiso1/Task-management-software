package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BugSearchRequest(
        @Size(max = 150, message = "Từ khóa tìm kiếm bug không được vượt quá 150 ký tự")
        String keyword,
        BugStatus status,
        BugSeverity severity,
        TaskPriority priority,
        UUID assigneeUserId,
        UUID reporterUserId,
        UUID taskId,
        UUID linkedTaskId,
        UUID backlogItemId,
        UUID sprintId,
        Boolean reopenedOnly,
        Boolean overdueOnly,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        Instant createdFrom,
        Instant createdTo
) {
}
