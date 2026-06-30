package com.project.taskmanagement.service.model;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;

import java.time.LocalDate;
import java.util.UUID;

public record TaskImportRowData(

        int rowNumber,

        UUID backlogItemId,

        String backlogItemTitle,

        String taskTitle,

        String description,

        TaskType type,

        TaskPriority priority,

        String assigneeEmail,

        Integer estimatedMinutes,

        LocalDate startDate,

        LocalDate dueDate

) {
}