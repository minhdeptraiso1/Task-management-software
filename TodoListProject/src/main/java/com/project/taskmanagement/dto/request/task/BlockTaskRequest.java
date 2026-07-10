package com.project.taskmanagement.dto.request.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlockTaskRequest(

        @NotBlank(
                message = "Lý do block không được để trống"
        )
        @Size(
                max = 1000,
                message = "Lý do block không được vượt quá 1000 ký tự"
        )
        String reason

) {
}
