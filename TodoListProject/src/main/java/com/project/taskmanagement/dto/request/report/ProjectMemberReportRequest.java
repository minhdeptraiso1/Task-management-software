package com.project.taskmanagement.dto.request.report;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectMemberReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        UUID userId

) {
}