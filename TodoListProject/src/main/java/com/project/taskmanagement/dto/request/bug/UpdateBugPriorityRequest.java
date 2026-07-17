package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.TaskPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateBugPriorityRequest(
        @NotNull
        TaskPriority priority
) {
}
