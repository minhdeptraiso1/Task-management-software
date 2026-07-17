package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;

import java.util.UUID;

public record BugSearchRequest(
        String keyword,
        BugStatus status,
        BugSeverity severity,
        TaskPriority priority,
        UUID assigneeUserId,
        UUID reporterUserId,
        UUID taskId,
        UUID backlogItemId,
        UUID sprintId
) {
}
