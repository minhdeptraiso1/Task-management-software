package com.project.taskmanagement.dto.request.taskcomment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTaskCommentRequest(

        @NotBlank(
                message = "Nội dung bình luận không được để trống"
        )
        @Size(
                max = 5000,
                message = "Nội dung bình luận không được vượt quá 5000 ký tự"
        )
        String content

) {
}