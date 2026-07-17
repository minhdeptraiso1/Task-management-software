package com.project.taskmanagement.dto.request.bug;

import com.project.taskmanagement.enums.BugStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBugStatusRequest(
        @NotNull
        BugStatus status
) {
}
