package com.project.taskmanagement.service.model;

import com.project.taskmanagement.entity.Task;

import java.util.List;
import java.util.UUID;

public record DailyDigestData(

        UUID userId,

        List<Task> overdueTasks,

        List<Task> dueTodayTasks,

        List<Task> dueSoonTasks,

        List<Task> blockedTasks,

        long unreadNotificationCount
) {

    public boolean hasContent() {
        return !overdueTasks.isEmpty()
                || !dueTodayTasks.isEmpty()
                || !dueSoonTasks.isEmpty()
                || !blockedTasks.isEmpty()
                || unreadNotificationCount > 0;
    }
}
