package com.project.taskmanagement.dto.response.projectmember;

import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(

        UUID id,

        UUID projectId,

        UUID userId,

        String username,

        String email,

        UserRole systemRole,

        ProjectMemberRole projectRole,

        Instant joinedAt,

        Instant createdAt

) {
}