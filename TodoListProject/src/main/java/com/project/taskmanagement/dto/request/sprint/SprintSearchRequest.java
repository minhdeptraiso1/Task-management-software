package com.project.taskmanagement.dto.request.sprint;

import com.project.taskmanagement.enums.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record SprintSearchRequest(

        @Schema(
                description = "Tìm theo tên hoặc mục tiêu Sprint",
                example = "authentication"
        )
        String keyword,

        @Schema(
                description = "Lọc theo trạng thái",
                example = "PLANNING"
        )
        SprintStatus status

) {
}