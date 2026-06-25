package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.response.notification.NotificationPageResponse;
import com.project.taskmanagement.dto.response.notification.NotificationResponse;
import com.project.taskmanagement.dto.response.notification.UnreadNotificationCountResponse;
import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.repository.projection.NotificationView;
import com.project.taskmanagement.service.NotificationQueryService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class NotificationQueryServiceImpl
        implements NotificationQueryService {

    NotificationRepository notificationRepository;
    NotificationRecipientRepository
            notificationRecipientRepository;

    CurrentUserService currentUserService;

    // ===================== LIST =====================

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getMyNotifications(
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Page<NotificationResponse> responsePage =
                notificationRecipientRepository
                        .findNotificationViewsByUserId(
                                currentUser.getId(),
                                pageable
                        )
                        .map(this::toResponse);

        return NotificationPageResponse.from(
                responsePage
        );
    }

    // ===================== UNREAD COUNT =====================

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount() {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        long unreadCount =
                notificationRecipientRepository
                        .countByUserIdAndReadAtIsNull(
                                currentUser.getId()
                        );

        return new UnreadNotificationCountResponse(
                unreadCount
        );
    }

    // ===================== MARK ONE AS READ =====================

    @Override
    @Transactional
    public NotificationResponse markAsRead(
            UUID notificationId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        NotificationRecipient recipient =
                notificationRecipientRepository
                        .findByNotificationIdAndUserId(
                                notificationId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.NOTIFICATION_NOT_FOUND
                                )
                        );

        if (recipient.getReadAt() == null) {
            recipient.setReadAt(
                    Instant.now()
            );

            notificationRecipientRepository.save(
                    recipient
            );
        }

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.NOTIFICATION_NOT_FOUND
                                )
                        );

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getActorUserId(),
                notification.getProjectId(),
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getCreatedAt(),
                recipient.getDeliveredAt(),
                recipient.getReadAt(),
                recipient.getReadAt() != null
        );
    }

    // ===================== MARK ALL AS READ =====================

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        notificationRecipientRepository
                .markAllAsRead(
                        currentUser.getId(),
                        Instant.now()
                );
    }

    // ===================== MAPPER =====================

    private NotificationResponse toResponse(
            NotificationView view
    ) {
        return new NotificationResponse(
                view.getId(),
                view.getType(),
                view.getTitle(),
                view.getContent(),
                view.getActorUserId(),
                view.getProjectId(),
                view.getEntityType(),
                view.getEntityId(),
                view.getCreatedAt(),
                view.getDeliveredAt(),
                view.getReadAt(),
                view.getReadAt() != null
        );
    }
}