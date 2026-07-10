package com.project.taskmanagement.dto.response.timesheet;

import java.time.LocalDate;

public record TimesheetDailySummaryResponse(

        LocalDate workDate,

        long totalMinutes,

        long logCount

) {
}
