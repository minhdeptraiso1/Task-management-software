package com.project.taskmanagement.dto.request.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjectRequest(

        @Schema(
                description = "Tên dự án",
                example = "Hệ thống quản lý công việc Agile Scrum"
        )
        @Size(
                max = 255,
                message = "Tên dự án không được vượt quá 255 ký tự"
        )
        String name,

        @Schema(
                description = "Mô tả dự án"
        )
        @Size(
                max = 5000,
                message = "Mô tả dự án không được vượt quá 5000 ký tự"
        )
        String description,

        @Schema(
                description = "Ngày bắt đầu",
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