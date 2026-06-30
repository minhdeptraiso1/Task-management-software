package com.project.taskmanagement.dto.request.taskcomment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTaskCommentRequest(

        @Schema(
                description = "ID comment cha nếu đây là reply"
        )
        UUID parentCommentId,

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