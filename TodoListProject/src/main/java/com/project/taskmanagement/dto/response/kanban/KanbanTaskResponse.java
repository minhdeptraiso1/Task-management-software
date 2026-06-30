package com.project.taskmanagement.dto.response.kanban;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;

import java.time.LocalDate;
import java.util.UUID;

public record KanbanTaskResponse(

        UUID id,

        UUID backlogItemId,

        String backlogItemTitle,

        String title,

        TaskType type,

        TaskStatus status,

        TaskPriority priority,

        UUID assigneeUserId,

        String assigneeUsername,

        String assigneeEmail,

        Integer estimatedMinutes,

        Long spentMinutes,

        LocalDate startDate,

        LocalDate dueDate,

        Long position,

        boolean overdue,

        boolean blocked

) {
}