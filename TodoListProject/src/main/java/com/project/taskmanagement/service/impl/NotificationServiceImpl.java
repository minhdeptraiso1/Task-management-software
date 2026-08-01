package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.realtime.RealtimeNotificationPayload;
import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.RealtimeNotificationService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.notification.NotificationTargetUrlResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class NotificationServiceImpl
        implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationRecipientRepository notificationRecipientRepository;
    RealtimeNotificationService realtimeNotificationService;
    NotificationTargetUrlResolver notificationTargetUrlResolver;

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true)
    public void create(
            NotificationCommand command
    ) {
        if (command == null) {
            return;
        }

        if (command.recipientUserIds() == null
                || command.recipientUserIds().isEmpty()) {
            return;
        }

        LinkedHashSet<UUID> uniqueRecipients =
                new LinkedHashSet<>(
                        command.recipientUserIds()
                );

        uniqueRecipients.remove(null);

        if (command.actorUserId() != null) {
            uniqueRecipients.remove(
                    command.actorUserId()
            );
        }

        if (uniqueRecipients.isEmpty()) {
            return;
        }

        if (command.dedupKey() != null
                && notificationRepository
                .existsByDedupKey(command.dedupKey())) {
            return;
        }

        Notification notification =
                Notification.builder()
                        .type(command.type())
                        .title(command.title())
                        .content(command.content())
                        .actorUserId(command.actorUserId())
                        .projectId(command.projectId())
                        .entityType(command.entityType())
                        .entityId(command.entityId())
                        .dedupKey(command.dedupKey())
                        .targetUrl(command.targetUrl())
                        .build();

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        Instant deliveredAt =
                Instant.now();

        List<NotificationRecipient> recipients =
                new ArrayList<>(
                        uniqueRecipients.size()
                );

        for (UUID userId : uniqueRecipients) {
            NotificationRecipient recipient =
                    NotificationRecipient.builder()
                            .notificationId(
                                    savedNotification.getId()
                            )
                            .userId(userId)
                            .deliveredAt(deliveredAt)
                            .readAt(null)
                            .build();

            recipients.add(recipient);
        }

        notificationRecipientRepository.saveAll(
                recipients
        );

        notificationRecipientRepository.flush();

        sendRealtimeNotification(
                savedNotification,
                uniqueRecipients
        );
    }

    private void sendRealtimeNotification(
            Notification notification,
            Iterable<UUID> recipientUserIds
    ) {
        if (notification == null
                || recipientUserIds == null) {
            return;
        }

        String targetUrl =
                notification.getTargetUrl() != null
                        ? notification.getTargetUrl()
                        : notificationTargetUrlResolver.resolve(
                        notification.getProjectId(),
                        notification.getEntityType(),
                        notification.getEntityId()
                );

        List<UUID> userIds =
                new ArrayList<>();

        for (UUID userId : recipientUserIds) {
            userIds.add(userId);
        }

        try {
            for (UUID userId : userIds) {
                RealtimeNotificationPayload payload =
                        new RealtimeNotificationPayload(
                                notification.getId(),
                                notification.getType(),
                                notification.getTitle(),
                                notification.getContent(),
                                notification.getProjectId(),
                                targetUrl,
                                notification.getActorUserId(),
                                notification.getCreatedAt(),
                                notificationRecipientRepository
                                        .countUnreadByUserId(userId)
                        );

                realtimeNotificationService.sendToUser(
                        userId,
                        payload
                );
            }
        } catch (RuntimeException ignored) {
            /*
             * Realtime is a fast delivery layer only.
             * Database + REST Notification API remain the source of truth.
             */
        }
    }
}
