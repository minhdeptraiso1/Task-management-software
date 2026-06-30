package com.project.taskmanagement.dto.response.tasktimelog;

import java.util.List;
import java.util.UUID;

public record TaskTimeSummaryResponse(

        UUID taskId,

        Integer estimatedMinutes,

        Long spentMinutes,

        Long remainingMinutes,

        Double progressPercentage,

        boolean overEstimated,

        Long overEstimatedMinutes,

        List<TaskTimeUserSummaryResponse> byUser

) {
}