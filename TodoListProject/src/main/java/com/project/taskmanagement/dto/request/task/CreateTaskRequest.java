package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(

        @Schema(
                description = "Backlog Item chứa Task"
        )
        @NotNull(
                message = "Backlog Item không được để trống"
        )
        UUID backlogItemId,

        @Schema(
                description = "Tiêu đề Task",
                example = "Xây dựng API đăng nhập"
        )
        @NotBlank(
                message = "Tiêu đề Task không được để trống"
        )
        @Size(
                max = 255,
                message = "Tiêu đề Task không được vượt quá 255 ký tự"
        )
        String title,

        @Size(
                max = 10000,
                message = "Mô tả không được vượt quá 10000 ký tự"
        )
        @Schema(
                description = "Mô tả chi tiết Task",
                example = "Xây dựng API đăng nhập bằng JWT access token và refresh token"
        )
        String description,

        @Schema(
                description = "Loại công việc",
                example = "DEVELOPMENT"
        )
        TaskType type,

        @Schema(
                description = "Mức độ ưu tiên của Task",
                example = "HIGH"
        )
        TaskPriority priority,

        @Schema(
                description = "User được phân công, có thể null"
        )
        UUID assigneeUserId,

        @Schema(
                description = "Thời gian dự kiến tính bằng phút",
                example = "480"
        )
        @Min(
                value = 0,
                message = "Thời gian dự kiến phải lớn hơn hoặc bằng 0"
        )
        Integer estimatedMinutes,

        @Schema(description = "Ngày bắt đầu dự kiến", example = "2026-08-04")
        LocalDate startDate,

        @Schema(description = "Ngày hết hạn dự kiến", example = "2026-08-10")
        LocalDate dueDate

) {
}
