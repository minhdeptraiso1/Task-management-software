package com.project.taskmanagement.dto.request.admin;

import com.project.taskmanagement.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(

        @NotNull
        UserRole role
) {
}
