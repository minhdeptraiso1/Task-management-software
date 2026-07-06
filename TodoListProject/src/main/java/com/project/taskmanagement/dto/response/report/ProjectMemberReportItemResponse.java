package com.project.taskmanagement.dto.response.report;

import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectMemberReportItemResponse(

        UUID userId,

        String username,

        String email,

        UserRole systemRole,

        ProjectMemberRole projectRole,

        long totalTasks,

        long activeTasks,

        long todoTasks,

        long inProgressTasks,

        long inReviewTasks,

        long doneTasks,

        long blockedTasks,

        long cancelledTasks,

        long overdueTasks,

        long estimatedMinutes,

        long spentMinutes,

        double completionRate,

        double timeUsageRate,

        boolean overEstimated,

        long overEstimatedMinutes,

        LocalDate firstWorkDate,

        LocalDate lastWorkDate

) {
}