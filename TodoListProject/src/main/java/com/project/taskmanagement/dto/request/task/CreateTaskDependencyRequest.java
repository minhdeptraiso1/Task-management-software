package com.project.taskmanagement.dto.request.task;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTaskDependencyRequest(

        @NotNull(
                message = "Task dependency không được để trống"
        )
        UUID dependsOnTaskId

) {
}
