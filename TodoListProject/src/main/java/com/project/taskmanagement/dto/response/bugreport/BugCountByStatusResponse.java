package com.project.taskmanagement.dto.response.bugreport;

import com.project.taskmanagement.enums.BugStatus;

public record BugCountByStatusResponse(
        BugStatus status,
        long total
) {
}
