package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.realtime.RealtimeNotificationPayload;

import java.util.Collection;
import java.util.UUID;

public interface RealtimeNotificationService {

    void sendToUser(
            UUID userId,
            RealtimeNotificationPayload payload
    );

    void sendToUsers(
            Collection<UUID> userIds,
            RealtimeNotificationPayload payload
    );
}
