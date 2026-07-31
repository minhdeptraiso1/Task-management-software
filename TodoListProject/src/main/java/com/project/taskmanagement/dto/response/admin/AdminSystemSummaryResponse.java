package com.project.taskmanagement.dto.response.admin;

import java.time.Instant;

public record AdminSystemSummaryResponse(

        Instant generatedAt,

        String timezone
) {
}
