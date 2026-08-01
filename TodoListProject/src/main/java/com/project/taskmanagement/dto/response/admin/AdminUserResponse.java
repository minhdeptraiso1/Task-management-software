package com.project.taskmanagement.dto.response.admin;

import com.project.taskmanagement.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(

        UUID id,

        String username,

        String email,

        UserRole role,

        boolean enabled,

        Instant createdAt,

        Instant updatedAt
) {
}
