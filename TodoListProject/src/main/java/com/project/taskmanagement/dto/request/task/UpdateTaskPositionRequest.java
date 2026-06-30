package com.project.taskmanagement.dto.request.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskPositionRequest(

        @NotNull(
                message = "Vị trí không được để trống"
        )
        @Min(
                value = 1,
                message = "Vị trí phải lớn hơn hoặc bằng 1"
        )
        Long position

) {
}