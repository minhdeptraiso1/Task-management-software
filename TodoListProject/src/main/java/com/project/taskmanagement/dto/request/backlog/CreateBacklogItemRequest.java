package com.project.taskmanagement.dto.request.backlog;

import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBacklogItemRequest(

        @Schema(
                description = "Tiêu đề Backlog Item",
                example = "Người dùng đăng nhập bằng email"
        )
        @NotBlank(
                message = "Tiêu đề không được để trống"
        )
        @Size(
                max = 255,
                message = "Tiêu đề không được vượt quá 255 ký tự"
        )
        String title,

        @Schema(
                description = "Mô tả yêu cầu"
        )
        @Size(
                max = 10000,
                message = "Mô tả không được vượt quá 10000 ký tự"
        )
        String description,

        @Schema(
                description = "Loại Backlog Item",
                example = "USER_STORY"
        )
        @NotNull(
                message = "Loại Backlog Item không được để trống"
        )
        BacklogItemType type,

        @Schema(
                description = "Độ ưu tiên",
                example = "HIGH"
        )
        BacklogPriority priority,

        @Schema(
                description = "Story Point",
                example = "5"
        )
        @Min(
                value = 0,
                message = "Story Point phải lớn hơn hoặc bằng 0"
        )
        Integer storyPoints

) {
}