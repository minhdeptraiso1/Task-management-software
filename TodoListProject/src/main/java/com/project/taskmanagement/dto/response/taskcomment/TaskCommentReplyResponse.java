package com.project.taskmanagement.dto.response.taskcomment;

import java.time.Instant;
import java.util.UUID;

public record TaskCommentReplyResponse(

        UUID id,

        UUID taskId,

        UUID userId,

        String username,

        String email,

        UUID parentCommentId,

        String content,

        Instant editedAt,

        Instant createdAt,

        Instant updatedAt,

        boolean canEdit,

        boolean canDelete

) {
}