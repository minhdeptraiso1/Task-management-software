package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintResponse(

        UUID id,

        UUID projectId,

        String name,

        String goal,

        SprintStatus status,

        LocalDate startDate,

        LocalDate endDate,

        Instant startedAt,

        Instant completedAt,

        UUID createdByUserId,

        long backlogItemCount,

        long taskCount,

        long completedTaskCount,

        double completionRate,

        Instant createdAt,

        Instant updatedAt

) {
}
