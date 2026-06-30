package com.project.taskmanagement.dto.response.kanban;

public record KanbanSummaryResponse(

        long total,

        long todo,

        long inProgress,

        long inReview,

        long done,

        long blocked,

        long cancelled

) {
}