package com.project.taskmanagement.dto.request.tasktimelog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskTimeLogRequest(

        LocalDate workDate,

        @Min(
                value = 1,
                message = "Số phút phải lớn hơn 0"
        )
        @Max(
                value = 720,
                message = "Một bản ghi không được vượt quá 720 phút"
        )
        Integer minutes,

        @Size(
                max = 2000,
                message = "Mô tả không được vượt quá 2000 ký tự"
        )
        String description

) {
}
