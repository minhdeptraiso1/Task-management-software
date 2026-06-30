package com.project.taskmanagement.dto.response.tasktimelog;

import java.util.UUID;

public record TaskTimeUserSummaryResponse(

        UUID userId,

        String username,

        String email,

        Long spentMinutes

) {
}