package com.project.taskmanagement.dto.response.taskstatistics;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintBurndownResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        long totalTasks,

        List<BurndownPointResponse> points

) {
}