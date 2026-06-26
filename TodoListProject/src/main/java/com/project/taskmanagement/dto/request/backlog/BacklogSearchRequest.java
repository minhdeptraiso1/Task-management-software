package com.project.taskmanagement.dto.request.backlog;

import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record BacklogSearchRequest(

        @Schema(
                description = "Tìm theo tiêu đề hoặc mô tả"
        )
        String keyword,

        BacklogItemStatus status,

        BacklogItemType type,

        BacklogPriority priority,

        @Schema(
                description = "Lọc theo Sprint ID"
        )
        UUID sprintId,

        @Schema(
                description = "Chỉ lấy item chưa nằm trong Sprint",
                example = "true"
        )
        Boolean unscheduledOnly

) {
}