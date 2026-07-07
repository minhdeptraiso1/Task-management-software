package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.SprintRiskSeverity;
import com.project.taskmanagement.enums.SprintRiskType;

import java.util.UUID;

public record SprintRiskResponse(

        SprintRiskType type,

        SprintRiskSeverity severity,

        String message,

        UUID taskId,

        String taskTitle,

        UUID userId,

        String username,

        String suggestedAction

) {
}
