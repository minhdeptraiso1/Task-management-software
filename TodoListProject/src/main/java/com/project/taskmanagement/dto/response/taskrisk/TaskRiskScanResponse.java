package com.project.taskmanagement.dto.response.taskrisk;

import java.time.Instant;
import java.util.UUID;

public record TaskRiskScanResponse(

        UUID projectId,

        long scannedTasks,

        long riskTasks,

        long highRiskTasks,

        long criticalRiskTasks,

        long notificationsCreated,

        Instant scannedAt

) {
}
