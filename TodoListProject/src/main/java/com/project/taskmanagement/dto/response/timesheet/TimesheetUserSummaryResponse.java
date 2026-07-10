package com.project.taskmanagement.dto.response.timesheet;

import java.util.UUID;

public record TimesheetUserSummaryResponse(

        UUID userId,

        String username,

        String email,

        long totalMinutes,

        long logCount,

        long taskCount

) {
}
