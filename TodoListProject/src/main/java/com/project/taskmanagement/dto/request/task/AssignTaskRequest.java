package com.project.taskmanagement.dto.request.task;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTaskRequest(

        @NotNull(
                message = "Người được phân công không được để trống"
        )
        UUID assigneeUserId

) {
}