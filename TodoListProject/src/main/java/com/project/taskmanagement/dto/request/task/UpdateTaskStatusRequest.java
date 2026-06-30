package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(

        @Schema(
                description = "Trạng thái mới của Task",
                example = "IN_PROGRESS"
        )
        @NotNull(
                message = "Trạng thái Task không được để trống"
        )
        TaskStatus status,

        @Schema(
                description = "Vị trí trong cột mới",
                example = "1"
        )
        @Min(
                value = 1,
                message = "Vị trí phải lớn hơn hoặc bằng 1"
        )
        Long position

) {
}