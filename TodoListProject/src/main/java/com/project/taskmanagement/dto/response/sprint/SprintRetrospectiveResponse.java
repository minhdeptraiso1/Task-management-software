package com.project.taskmanagement.dto.response.sprint;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SprintRetrospectiveResponse(

        UUID id,

        UUID projectId,

        UUID sprintId,

        String wentWell,

        String wentWrong,

        String improvement,

        List<SprintRetroActionItemResponse> actionItems,

        String note,

        Instant createdAt,

        Instant updatedAt

) {
}
