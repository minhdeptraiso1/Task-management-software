package com.project.taskmanagement.dto.request.task;

import com.project.taskmanagement.enums.TaskStatus;

public record UnblockTaskRequest(

        TaskStatus targetStatus

) {
}
