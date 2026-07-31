package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.realtime.RealtimeNotificationPayload;
import com.project.taskmanagement.service.RealtimeNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class RealtimeNotificationServiceImpl
        implements RealtimeNotificationService {

    static final String NOTIFICATION_DESTINATION =
            "/queue/notifications";

    SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToUser(
            UUID userId,
            RealtimeNotificationPayload payload
    ) {
        if (userId == null
                || payload == null) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                NOTIFICATION_DESTINATION,
                payload
        );
    }

    @Override
    public void sendToUsers(
            Collection<UUID> userIds,
            RealtimeNotificationPayload payload
    ) {
        if (userIds == null
                || userIds.isEmpty()
                || payload == null) {
            return;
        }

        Set<UUID> uniqueUserIds =
                new HashSet<>(userIds);

        uniqueUserIds.remove(null);

        for (UUID userId : uniqueUserIds) {
            sendToUser(userId, payload);
        }
    }
}
