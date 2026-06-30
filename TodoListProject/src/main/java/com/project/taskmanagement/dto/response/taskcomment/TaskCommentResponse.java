package com.project.taskmanagement.dto.response.taskcomment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskCommentResponse(

        UUID id,

        UUID taskId,

        UUID userId,

        String username,

        String email,

        String content,

        Instant editedAt,

        Instant createdAt,

        Instant updatedAt,

        boolean canEdit,

        boolean canDelete,

        int replyCount,

        List<TaskCommentReplyResponse> replies

) {
}