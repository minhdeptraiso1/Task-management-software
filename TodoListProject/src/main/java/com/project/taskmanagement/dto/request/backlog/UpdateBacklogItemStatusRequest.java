package com.project.taskmanagement.dto.request.backlog;

import com.project.taskmanagement.enums.BacklogItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateBacklogItemStatusRequest(

        @Schema(
                description = "Trạng thái mới",
                example = "READY"
        )
        @NotNull(
                message = "Trạng thái không được để trống"
        )
        BacklogItemStatus status

) {
}