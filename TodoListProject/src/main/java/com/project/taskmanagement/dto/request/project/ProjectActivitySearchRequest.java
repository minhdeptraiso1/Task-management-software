package com.project.taskmanagement.dto.request.project;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;

import java.time.Instant;
import java.util.UUID;

public record ProjectActivitySearchRequest(

        ActivityEntityType entityType,

        ProjectActivityAction action,

        UUID performedByUserId,

        Instant fromDate,

        Instant toDate,

        String keyword

) {
}