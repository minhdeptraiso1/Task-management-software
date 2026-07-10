package com.project.taskmanagement.dto.request.tasktimelog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskTimeLogRequest(

        @Schema(
                description = "Ngày thực hiện công việc",
                example = "2026-07-03"
        )
        @NotNull(
                message = "Ngày làm việc không được để trống"
        )
        LocalDate workDate,

        @Schema(
                description = "Thời gian làm việc tính bằng phút",
                example = "120"
        )
        @NotNull(
                message = "Số phút làm việc không được để trống"
        )
        @Min(
                value = 1,
                message = "Số phút phải lớn hơn 0"
        )
        @Max(
                value = 720,
                message = "Một bản ghi không được vượt quá 720 phút"
        )
        Integer minutes,

        @Schema(
                description = "Mô tả công việc đã thực hiện"
        )
        @Size(
                max = 2000,
                message = "Mô tả không được vượt quá 2000 ký tự"
        )
        String description

) {
}
