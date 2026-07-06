package com.project.taskmanagement.dto.request.dashboard;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;

public record MyTaskSearchRequest(

        String keyword,

        TaskStatus status,

        TaskPriority priority,

        Boolean overdueOnly,

        Boolean dueSoonOnly

) {
}