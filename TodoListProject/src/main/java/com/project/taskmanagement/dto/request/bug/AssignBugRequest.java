package com.project.taskmanagement.dto.request.bug;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignBugRequest(
        @NotNull
        UUID assigneeUserId
) {
}
