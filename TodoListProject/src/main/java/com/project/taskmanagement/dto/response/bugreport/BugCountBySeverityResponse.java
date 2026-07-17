package com.project.taskmanagement.dto.response.bugreport;

import com.project.taskmanagement.enums.BugSeverity;

public record BugCountBySeverityResponse(
        BugSeverity severity,
        long total
) {
}
