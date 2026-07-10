package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReopenTaskRequest(

        @NotNull(
                message = "Trạng thái reopen không được để trống"
        )
        TaskStatus targetStatus,

        @Size(
                max = 1000,
                message = "Lý do reopen không được vượt quá 1000 ký tự"
        )
        String reason

) {
}
