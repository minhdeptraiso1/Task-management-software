package com.project.taskmanagement.dto.response.dashboard;

import com.project.taskmanagement.enums.TaskStatus;

public record ProjectDashboardTaskStatusResponse(

        TaskStatus status,

        String title,

        long count,

        double percentage

) {
}