package com.project.taskmanagement.dto.response.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BurnupChartResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        LocalDate startDate,

        LocalDate endDate,

        List<BurnupPointResponse> points
) {
}
