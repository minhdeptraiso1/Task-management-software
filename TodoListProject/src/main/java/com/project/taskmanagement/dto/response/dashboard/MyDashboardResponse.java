package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.dto.response.taskrisk.MyTaskRiskSummaryResponse;
import com.project.taskmanagement.enums.UserRole;

import java.util.List;
import java.util.UUID;

public record MyDashboardResponse(

        UUID userId,

        String username,

        String email,

        UserRole systemRole,

        boolean adminDashboard,

        long projectCount,

        long unreadNotifications,

        MyTaskSummaryResponse taskSummary,

        MyTaskRiskSummaryResponse riskSummary,

        MyTimeSummaryResponse timeSummary,

        List<MyUpcomingTaskResponse> overdueTasks,

        List<MyUpcomingTaskResponse> upcomingTasks

) {
}
