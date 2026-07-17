package com.project.taskmanagement.dto.request.bug;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBugCommentRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {
}
