package com.project.taskmanagement.dto.response.kanban;

import com.project.taskmanagement.enums.TaskStatus;

import java.util.List;

public record KanbanColumnResponse(

        TaskStatus status,

        String title,

        int taskCount,

        List<KanbanTaskResponse> tasks

) {
}