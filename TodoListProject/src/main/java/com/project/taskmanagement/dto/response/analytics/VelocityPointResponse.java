package com.project.taskmanagement.dto.response.analytics;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.UUID;

public record VelocityPointResponse(

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        long committedItems,

        long completedItems,

        long committedStoryPoints,

        long completedStoryPoints,

        double completionRate
) {
}
