package com.project.taskmanagement.dto.response.sprint;

import java.time.Instant;
import java.util.UUID;

public record SprintReviewResponse(

        UUID id,

        UUID projectId,

        UUID sprintId,

        Boolean goalAchieved,

        String demoSummary,

        String stakeholderFeedback,

        String acceptedItemSummary,

        String rejectedItemSummary,

        String note,

        Instant createdAt,

        Instant updatedAt

) {
}
