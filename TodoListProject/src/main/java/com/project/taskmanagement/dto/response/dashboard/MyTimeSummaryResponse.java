package com.project.taskmanagement.dto.response.dashboard;

import java.time.LocalDate;

public record MyTimeSummaryResponse(

        LocalDate currentDate,

        LocalDate weekStartDate,

        LocalDate weekEndDate,

        LocalDate monthStartDate,

        LocalDate monthEndDate,

        long todayMinutes,

        long weekMinutes,

        long monthMinutes

) {
}