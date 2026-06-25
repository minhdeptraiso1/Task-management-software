package com.project.taskmanagement.dto.request.projectmember;

import com.project.taskmanagement.enums.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRoleRequest(

        @Schema(
                description = "Vai trò mới trong dự án",
                example = "TESTER"
        )
        @NotNull(
                message = "Vai trò mới không được để trống"
        )
        ProjectMemberRole role

) {
}
