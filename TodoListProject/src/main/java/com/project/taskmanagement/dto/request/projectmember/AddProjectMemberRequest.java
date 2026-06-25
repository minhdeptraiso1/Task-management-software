package com.project.taskmanagement.dto.request.projectmember;

import com.project.taskmanagement.enums.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(

        @Schema(
                description = "ID tài khoản được thêm vào dự án"
        )
        @NotNull(
                message = "User ID không được để trống"
        )
        UUID userId,

        @Schema(
                description = "Vai trò của thành viên trong dự án",
                example = "DEVELOPER"
        )
        @NotNull(
                message = "Vai trò dự án không được để trống"
        )
        ProjectMemberRole role

) {
}