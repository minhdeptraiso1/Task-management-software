package com.project.taskmanagement.dto.request.bug;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBugCommentRequest(
        UUID parentId,
        @NotBlank
        @Size(max = 5000)
        String content
) {
}
