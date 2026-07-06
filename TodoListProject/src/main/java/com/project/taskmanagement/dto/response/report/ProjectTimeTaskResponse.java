package com.project.taskmanagement.dto.response.report;

import com.project.taskmanagement.enums.TaskStatus;

import java.util.UUID;

public record ProjectTimeTaskResponse(

        UUID taskId,

        String taskTitle,

        TaskStatus taskStatus,

        UUID assigneeUserId,

        Integer estimatedMinutes,

        long spentMinutes,

        long logCount,

        long contributorCount,

        double timeUsageRate,

        boolean overEstimated,

        long overEstimatedMinutes,

        String targetUrl

) {
}