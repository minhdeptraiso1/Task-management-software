package com.project.taskmanagement.dto.response.analytics;

import java.util.List;
import java.util.UUID;

public record VelocityChartResponse(

        UUID projectId,

        String projectCode,

        String projectName,

        long averageCompletedItems,

        long averageCompletedStoryPoints,

        List<VelocityPointResponse> points
) {
}
