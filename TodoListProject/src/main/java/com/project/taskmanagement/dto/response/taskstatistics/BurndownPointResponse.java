package com.project.taskmanagement.dto.response.taskstatistics;

import java.time.LocalDate;

public record BurndownPointResponse(

        LocalDate date,

        double idealRemainingTasks,

        long actualRemainingTasks,

        long completedTasks,

        long completedOnDate

) {
}