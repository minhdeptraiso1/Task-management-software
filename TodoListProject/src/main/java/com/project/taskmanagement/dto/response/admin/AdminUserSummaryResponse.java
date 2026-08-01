package com.project.taskmanagement.dto.response.admin;

public record AdminUserSummaryResponse(

        long totalUsers,

        long activeUsers,

        long disabledUsers,

        long adminUsers,

        long managerUsers,

        long employeeUsers
) {
}
