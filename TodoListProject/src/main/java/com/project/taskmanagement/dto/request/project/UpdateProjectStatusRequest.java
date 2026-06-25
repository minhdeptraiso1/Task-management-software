package com.project.taskmanagement.dto.request.project;

import com.project.taskmanagement.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectStatusRequest(

        @Schema(
                description = "Trạng thái mới của dự án",
                example = "ACTIVE"
        )
        @NotNull(
                message = "Trạng thái dự án không được để trống"
        )
        ProjectStatus status

) {
}