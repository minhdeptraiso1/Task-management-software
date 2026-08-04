package com.project.taskmanagement.dto.request.taskcomment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTaskCommentRequest(

        @Schema(
                description = "ID comment cha nếu đây là reply; để trống khi tạo bình luận gốc",
                example = "019f1651-96b2-72bc-95a3-d5903f79d358"
        )
        UUID parentCommentId,

        @NotBlank(
                message = "Nội dung bình luận không được để trống"
        )
        @Size(
                max = 5000,
                message = "Nội dung bình luận không được vượt quá 5000 ký tự"
        )
        @Schema(
                description = "Nội dung bình luận; có thể chứa mention theo cú pháp hệ thống",
                example = "@Dev_Minh vui lòng cập nhật bằng chứng kiểm thử trước khi review."
        )
        String content

) {
}
