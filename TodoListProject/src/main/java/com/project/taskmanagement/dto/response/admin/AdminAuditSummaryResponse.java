package com.project.taskmanagement.dto.response.admin;

import java.time.LocalDate;
import java.util.List;

public record AdminAuditSummaryResponse(

        long totalLogs,

        long authLogs,

        long userLogs,

        long projectLogs,

        long fileLogs,

        long importLogs,

        List<AdminAuditSummaryItemResponse> byAction,

        List<AdminAuditSummaryItemResponse> byResourceType,

        List<AdminAuditDailySummaryResponse> byDate
) {

    public record AdminAuditSummaryItemResponse(

            String name,

            long count
    ) {
    }

    public record AdminAuditDailySummaryResponse(

            LocalDate date,

            long count
    ) {
    }
}
