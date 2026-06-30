package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;

import java.util.UUID;

public record TaskSearchRequest(

        String keyword,

        UUID backlogItemId,

        UUID sprintId,

        UUID assigneeUserId,

        TaskStatus status,

        TaskPriority priority,

        TaskType type,

        Boolean unassignedOnly

) {
}