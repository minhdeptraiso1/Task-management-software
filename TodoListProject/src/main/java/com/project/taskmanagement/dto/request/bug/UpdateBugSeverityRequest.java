package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.BugSeverity;
import jakarta.validation.constraints.NotNull;

public record UpdateBugSeverityRequest(
        @NotNull
        BugSeverity severity
) {
}
