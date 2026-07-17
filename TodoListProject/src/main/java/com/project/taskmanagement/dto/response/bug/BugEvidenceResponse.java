package com.project.taskmanagement.dto.response.bug;

import java.time.Instant;
import java.util.UUID;

public record BugEvidenceResponse(
        UUID id,
        UUID bugId,
        UUID createdByUserId,
        String createdByUsername,
        String title,
        String stepsToReproduce,
        String expectedResult,
        String actualResult,
        String environment,
        String note,
        boolean canEdit,
        boolean canDelete,
        Instant createdAt,
        Instant updatedAt
) {
}
