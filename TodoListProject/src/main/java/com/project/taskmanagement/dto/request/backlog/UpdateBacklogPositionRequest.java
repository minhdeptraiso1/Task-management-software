package com.project.taskmanagement.dto.request.backlog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateBacklogPositionRequest(

        @Schema(
                description = "Vị trí mới của Backlog Item",
                example = "2"
        )
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