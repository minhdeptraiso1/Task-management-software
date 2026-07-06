package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectDashboardProjectResponse(

        UUID projectId,

        String code,

        String name,

        String description,

        ProjectStatus status,

        LocalDate startDate,

        LocalDate endDate,

        long memberCount

) {
}