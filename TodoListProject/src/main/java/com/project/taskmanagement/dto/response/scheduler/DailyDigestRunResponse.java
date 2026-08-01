package com.project.taskmanagement.dto.response.scheduler;

import java.time.LocalDate;

public record DailyDigestRunResponse(

        LocalDate businessDate,

        long scannedUsers,

        long sentDigests,

        long skippedUsers
) {
}
