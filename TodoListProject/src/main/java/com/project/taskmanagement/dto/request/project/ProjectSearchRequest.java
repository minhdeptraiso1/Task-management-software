package com.project.taskmanagement.dto.request.project;

import com.project.taskmanagement.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

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

        LocalDate startDateFrom,

        LocalDate startDateTo,

        LocalDate endDateFrom,

        LocalDate endDateTo,

        Instant createdFrom,

        Instant createdTo

) {
}
