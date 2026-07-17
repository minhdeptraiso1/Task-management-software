package com.project.taskmanagement.dto.response.bug;

import java.time.Instant;
import java.util.UUID;

public record BugCommentResponse(
        UUID id,
        UUID bugId,
        UUID parentId,
        UUID authorUserId,
        String authorUsername,
        String authorEmail,
        String content,
        boolean canEdit,
        boolean canDelete,
        Instant createdAt,
        Instant updatedAt
) {
}
