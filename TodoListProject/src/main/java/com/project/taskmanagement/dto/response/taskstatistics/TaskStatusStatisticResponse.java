package com.project.taskmanagement.dto.response.taskstatistics;

import com.project.taskmanagement.enums.TaskStatus;

public record TaskStatusStatisticResponse(

        TaskStatus status,

        String title,

        long count,

        double percentage

) {
}