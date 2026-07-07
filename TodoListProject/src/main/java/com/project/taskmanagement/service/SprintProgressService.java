package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.sprint.SprintProgressResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReminderResponse;

import java.util.UUID;

public interface SprintProgressService {

    SprintProgressResponse getProgress(
            UUID projectId,
            UUID sprintId
    );

    SprintReminderResponse remindEndingSoon(
            UUID projectId,
            UUID sprintId
    );

    SprintReminderResponse remindOverdueTasks(
            UUID projectId,
            UUID sprintId
    );

    SprintReminderResponse remindBlockedTasks(
            UUID projectId,
            UUID sprintId
    );
}
