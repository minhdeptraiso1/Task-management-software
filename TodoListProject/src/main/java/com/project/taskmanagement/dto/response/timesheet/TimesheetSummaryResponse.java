package com.project.taskmanagement.dto.response.timesheet;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TimesheetSummaryResponse(

        UUID projectId,

        UUID userId,

        LocalDate fromDate,

        LocalDate toDate,

        long totalMinutes,

        long totalLogs,

        long taskCount,

        long userCount,

        List<TimesheetDailySummaryResponse> byDate,

        List<TimesheetUserSummaryResponse> byUser

) {
}
