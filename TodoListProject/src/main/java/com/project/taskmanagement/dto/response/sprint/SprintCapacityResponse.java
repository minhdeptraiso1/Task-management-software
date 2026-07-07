package com.project.taskmanagement.dto.response.sprint;

import com.project.taskmanagement.enums.SprintStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintCapacityResponse(

        UUID projectId,

        UUID sprintId,

        String sprintName,

        SprintStatus sprintStatus,

        LocalDate startDate,

        LocalDate endDate,

        long sprintDays,

        long memberCount,

        long totalCapacityMinutes,

        long totalEstimatedMinutes,

        long totalSpentMinutes,

        long remainingCapacityMinutes,

        double utilizationRate,

        boolean overCapacity,

        long overCapacityMinutes,

        List<SprintCapacityMemberResponse> members

) {
}
