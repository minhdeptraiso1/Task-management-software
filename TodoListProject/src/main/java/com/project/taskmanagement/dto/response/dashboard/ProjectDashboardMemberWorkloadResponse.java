package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;

import java.util.UUID;

public record ProjectDashboardMemberWorkloadResponse(

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

        boolean overEstimated,

        long overEstimatedMinutes

) {
}