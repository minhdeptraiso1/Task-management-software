package com.project.taskmanagement.dto.response.analytics;

import java.time.LocalDate;

public record BurnupPointResponse(

        LocalDate date,

        long totalScope,

        long completedScope,

        double completionRate
) {
}
