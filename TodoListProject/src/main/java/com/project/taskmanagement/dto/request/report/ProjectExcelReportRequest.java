package com.project.taskmanagement.dto.request.report;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectExcelReportRequest(
        LocalDate fromDate,
        LocalDate toDate,
        UUID sprintId,
        UUID userId
) {
}
