package com.project.taskmanagement.dto.request.project;

import com.project.taskmanagement.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectSearchRequest(

        @Schema(
                description = "Tìm theo mã hoặc tên dự án",
                example = "TASK"
        )
        @Size(max = 100, message = "Từ khóa tìm kiếm dự án không được vượt quá 100 ký tự")
        String keyword,

        @Schema(
                description = "Lọc theo trạng thái dự án",
                example = "ACTIVE"
        )
        ProjectStatus status,

        @Schema(
                description = "Lọc các dự án được giao cho tài khoản MANAGER",
                example = "14f39a47-5018-4d11-ae90-8f2db3daf63a"
        )
        UUID managerUserId,

        LocalDate startDateFrom,

        LocalDate startDateTo,

        LocalDate endDateFrom,

        LocalDate endDateTo,

        Instant createdFrom,

        Instant createdTo

) {
}
