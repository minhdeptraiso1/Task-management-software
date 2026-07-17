package com.project.taskmanagement.dto.response.bugreport;

import com.project.taskmanagement.enums.TaskPriority;

public record BugCountByPriorityResponse(
        TaskPriority priority,
        long total
) {
}
