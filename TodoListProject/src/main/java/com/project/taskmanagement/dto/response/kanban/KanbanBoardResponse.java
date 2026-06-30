package com.project.taskmanagement.dto.response.kanban;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record KanbanBoardResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        KanbanSummaryResponse summary,

        List<KanbanColumnResponse> columns

) {
}