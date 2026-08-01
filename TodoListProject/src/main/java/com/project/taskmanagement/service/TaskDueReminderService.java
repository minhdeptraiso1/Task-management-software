package com.project.taskmanagement.service;

import com.project.taskmanagement.service.model.SchedulerResult;

public interface TaskDueReminderService {

    SchedulerResult runDailyReminders();

    void sendDueSoonReminders();

    void sendOverdueReminders();
}
