package com.project.taskmanagement.dto.response.project;

import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(

        UUID id,

        String code,

        String name,

        String description,

        ProjectStatus status,

        LocalDate startDate,

        LocalDate endDate,

        UUID createdByUserId,

        ProjectMemberRole currentUserRole,

        Instant createdAt,

        Instant updatedAt

) {
}