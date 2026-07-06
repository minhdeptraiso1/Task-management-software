package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectDashboardSprintResponse(

        UUID sprintId,

        String name,

        String goal,

        SprintStatus status,

        LocalDate startDate,

        LocalDate endDate,

        Instant startedAt,

        Instant completedAt,

        long backlogItemCount,

        long taskCount,

        long completedTaskCount,

        double completionRate

) {
}