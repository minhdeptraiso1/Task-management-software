package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.TaskPriority;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateBugRequest(
        UUID backlogItemId,
        UUID taskId,
        UUID sprintId,
        @Size(max = 255)
        String title,
        @Size(max = 5000)
        String description,
        BugSeverity severity,
        TaskPriority priority,
        @Size(max = 5000)
        String reproductionSteps,
        @Size(max = 5000)
        String expectedResult,
        @Size(max = 5000)
        String actualResult,
        LocalDate dueDate
) {
}
