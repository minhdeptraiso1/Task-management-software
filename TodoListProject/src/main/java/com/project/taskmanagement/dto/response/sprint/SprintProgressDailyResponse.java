package com.project.taskmanagement.dto.response.sprint;

import java.time.LocalDate;

public record SprintProgressDailyResponse(

        LocalDate date,

        long completedOnDate,

        long cumulativeCompletedTasks,

        long remainingTasks,

        double idealRemainingTasks

) {
}
