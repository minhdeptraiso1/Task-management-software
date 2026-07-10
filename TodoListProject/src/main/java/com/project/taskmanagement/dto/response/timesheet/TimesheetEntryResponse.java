package com.project.taskmanagement.dto.response.timesheet;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TimesheetEntryResponse(

        UUID id,

        UUID projectId,

        String projectCode,

        String projectName,

        UUID taskId,

        String taskTitle,

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
