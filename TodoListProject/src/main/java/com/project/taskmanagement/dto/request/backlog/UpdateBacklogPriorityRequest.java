package com.project.taskmanagement.dto.request.backlog;

import com.project.taskmanagement.enums.BacklogPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateBacklogPriorityRequest(

        @Schema(
                description = "Độ ưu tiên mới",
                example = "URGENT"
        )
        @NotNull(
                message = "Độ ưu tiên không được để trống"
        )
        BacklogPriority priority

) {
}