package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintProgressResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate today,

        long totalDays,

        long elapsedDays,

        long daysRemaining,

        long totalTasks,

        long completedTasks,

        long unfinishedTasks,

        long blockedTasks,

        long overdueTasks,

        double actualCompletionRate,

        double expectedProgressRate,

        double progressGap,

        boolean behindSchedule,

        boolean endingSoon,

        boolean overdueSprint,

        List<SprintProgressDailyResponse> dailyProgress

) {
}
