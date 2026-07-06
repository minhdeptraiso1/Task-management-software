package com.project.taskmanagement.dto.response.report;

import java.time.LocalDate;

public record ProjectTimeDailyResponse(

        LocalDate workDate,

        long spentMinutes,

        long logCount

) {
}