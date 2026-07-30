package com.project.taskmanagement.dto.response.analytics;

import java.util.List;
import java.util.UUID;

public record CumulativeFlowResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        List<CumulativeFlowPointResponse> points
) {
}
