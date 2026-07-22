package com.project.taskmanagement.dto.request.project;

import com.project.taskmanagement.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

public record ProjectSearchRequest(

        @Schema(
                description = "Tìm theo mã hoặc tên dự án",
                example = "TASK"
        )
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
