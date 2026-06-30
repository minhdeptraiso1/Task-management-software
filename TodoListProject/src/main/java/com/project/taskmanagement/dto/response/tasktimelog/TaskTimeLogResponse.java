package com.project.taskmanagement.dto.response.tasktimelog;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskTimeLogResponse(

        UUID id,

        UUID taskId,

        UUID userId,

        String username,

        String email,

        LocalDate workDate,

        Integer minutes,

        String description,

        boolean canEdit,

        boolean canDelete,

        Instant createdAt,

        Instant updatedAt

) {
}