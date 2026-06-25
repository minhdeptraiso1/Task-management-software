package com.project.taskmanagement.service;

import com.project.taskmanagement.service.model.NotificationCommand;

public interface NotificationService {
    void create(NotificationCommand command);
}