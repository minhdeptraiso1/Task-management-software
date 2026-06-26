package com.project.taskmanagement.dto.request.sprint;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateSprintRequest(

        @Schema(
                description = "Tên Sprint mới",
                example = "Sprint 1 - Authentication"
        )
        @Size(
                max = 255,
                message = "Tên Sprint không được vượt quá 255 ký tự"
        )
        String name,

        @Schema(
                description = "Mục tiêu Sprint mới"
        )
        @Size(
                max = 5000,
                message = "Mục tiêu Sprint không được vượt quá 5000 ký tự"
        )
        String goal,

        @Schema(
                description = "Ngày bắt đầu mới",
                example = "2026-07-01"
        )
        LocalDate startDate,

        @Schema(
                description = "Ngày kết thúc mới",
                example = "2026-07-14"
        )
        LocalDate endDate

) {
}