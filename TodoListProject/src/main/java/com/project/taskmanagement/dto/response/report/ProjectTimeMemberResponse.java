package com.project.taskmanagement.dto.response.report;

import java.util.UUID;

public record ProjectTimeMemberResponse(

        UUID userId,

        String username,

        String email,

        long spentMinutes,

        long logCount,

        long taskCount,

        double percentage

) {
}