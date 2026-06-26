package com.project.taskmanagement.dto.request.backlog;

import com.project.taskmanagement.enums.BacklogItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateBacklogItemRequest(

        @Schema(
                description = "Tiêu đề mới"
        )
        @Size(
                max = 255,
                message = "Tiêu đề không được vượt quá 255 ký tự"
        )
        String title,

        @Schema(
                description = "Mô tả mới"
        )
        @Size(
                max = 10000,
                message = "Mô tả không được vượt quá 10000 ký tự"
        )
        String description,

        @Schema(
                description = "Loại Backlog Item mới",
                example = "FEATURE"
        )
        BacklogItemType type,

        @Schema(
                description = "Story Point mới",
                example = "8"
        )
        @Min(
                value = 0,
                message = "Story Point phải lớn hơn hoặc bằng 0"
        )
        Integer storyPoints

) {
}