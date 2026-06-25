package com.project.taskmanagement.dto.request.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(

        @Schema(
                description = "Mã dự án",
                example = "TASK-MANAGEMENT"
        )
        @NotBlank(
                message = "Mã dự án không được để trống"
        )
        @Size(
                max = 50,
                message = "Mã dự án không được vượt quá 50 ký tự"
        )
        String code,

        @Schema(
                description = "Tên dự án",
                example = "Hệ thống quản lý công việc Agile Scrum"
        )
        @NotBlank(
                message = "Tên dự án không được để trống"
        )
        @Size(
                max = 255,
                message = "Tên dự án không được vượt quá 255 ký tự"
        )
        String name,

        @Schema(
                description = "Mô tả dự án",
                example = "Hệ thống quản lý công việc cho văn phòng IT"
        )
        @Size(
                max = 5000,
                message = "Mô tả dự án không được vượt quá 5000 ký tự"
        )
        String description,

        @Schema(
                description = "Ngày bắt đầu dự án",
                example = "2026-07-01"
        )
        LocalDate startDate,

        @Schema(
                description = "Ngày kết thúc dự kiến",
                example = "2026-12-31"
        )
        LocalDate endDate

) {
}