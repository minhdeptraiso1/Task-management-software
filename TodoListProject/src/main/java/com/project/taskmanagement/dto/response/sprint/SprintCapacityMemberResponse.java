package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;

import java.util.UUID;

public record SprintCapacityMemberResponse(

        UUID userId,

        String username,

        String email,

        UserRole systemRole,

        ProjectMemberRole projectRole,

        long capacityMinutes,

        long assignedEstimatedMinutes,

        long spentMinutes,

        long remainingCapacityMinutes,

        double utilizationRate,

        boolean overCapacity,

        long overCapacityMinutes,

        long totalTasks,

        long todoTasks,

        long inProgressTasks,

        long inReviewTasks,

        long doneTasks,

        long blockedTasks,

        long cancelledTasks

) {
}
