package com.project.taskmanagement.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectAiAskRequest(
        @NotBlank(message = "Câu hỏi AI không được để trống")
        @Size(min = 5, max = 1000, message = "Câu hỏi phải từ 5 đến 1000 ký tự")
        String question,

        @Size(max = 1000, message = "Ghi chú bổ sung không được vượt quá 1000 ký tự")
        String additionalContext) {
}
