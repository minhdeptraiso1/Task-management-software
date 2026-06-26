package com.project.taskmanagement.dto.request.sprint;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateSprintRequest(

        @Schema(
                description = "Tên Sprint",
                example = "Sprint 1"
        )
        @NotBlank(
                message = "Tên Sprint không được để trống"
        )
        @Size(
                max = 255,
                message = "Tên Sprint không được vượt quá 255 ký tự"
        )
        String name,

        @Schema(
                description = "Mục tiêu Sprint",
                example = "Hoàn thiện chức năng xác thực và quản lý tài khoản"
        )
        @Size(
                max = 5000,
                message = "Mục tiêu Sprint không được vượt quá 5000 ký tự"
        )
        String goal,

        @Schema(
                description = "Ngày bắt đầu dự kiến",
                example = "2026-07-01"
        )
        LocalDate startDate,

        @Schema(
                description = "Ngày kết thúc dự kiến",
                example = "2026-07-14"
        )
        LocalDate endDate

) {
}