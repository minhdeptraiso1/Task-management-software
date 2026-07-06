package com.project.taskmanagement.dto.request.report;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectTimeReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        UUID userId,

        UUID taskId

) {
}