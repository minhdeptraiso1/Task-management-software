package com.project.taskmanagement.dto.request.bug;

import jakarta.validation.constraints.Size;

public record UpdateBugEvidenceRequest(
        @Size(max = 255)
        String title,
        @Size(max = 5000)
        String stepsToReproduce,
        @Size(max = 5000)
        String expectedResult,
        @Size(max = 5000)
        String actualResult,
        @Size(max = 2000)
        String environment,
        @Size(max = 2000)
        String note
) {
}
