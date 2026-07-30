package com.project.taskmanagement.dto.response.analytics;

import java.time.LocalDate;

public record CumulativeFlowPointResponse(

        LocalDate date,

        long todo,

        long inProgress,

        long inReview,

        long blocked,

        long done,

        long cancelled
) {
}
