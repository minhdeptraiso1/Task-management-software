package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @Size(
                max = 255,
                message = "Tiêu đề Task không được vượt quá 255 ký tự"
        )
        String title,

        @Size(
                max = 10000,
                message = "Mô tả không được vượt quá 10000 ký tự"
        )
        String description,

        TaskType type,

        TaskPriority priority,

        @Schema(
                description = "Thời gian dự kiến tính bằng phút"
        )
        @Min(
                value = 0,
                message = "Thời gian dự kiến phải lớn hơn hoặc bằng 0"
        )
        Integer estimatedMinutes,

        LocalDate startDate,

        LocalDate dueDate

) {
}