package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.notification.NotificationSearchRequest;
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
import com.project.taskmanagement.security.CurrentUser;
import com.project.taskmanagement.service.NotificationQueryService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.notification.NotificationTargetUrlResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
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

    NotificationTargetUrlResolver
            notificationTargetUrlResolver;

    // ===================== LIST =====================

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getMyNotifications(
            NotificationSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        NotificationSearchRequest safeRequest =
                request == null
                        ? new NotificationSearchRequest(
                        null,
                        null
                )
                        : request;

        Page<NotificationResponse> responsePage =
                notificationRecipientRepository
                        .searchNotificationViews(
                                currentUser.getId(),
                                safeRequest.type(),
                                safeRequest.unread(),
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
                        .countUnreadByUserId(
                                currentUser.getId()
                        );

        return new UnreadNotificationCountResponse(
                unreadCount
        );
    }

    // ===================== MARK ONE AS READ =====================

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true)
    public NotificationResponse markAsRead(
            UUID notificationId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        NotificationRecipient recipient =
                getOwnedRecipientOrThrow(
                        notificationId,
                        currentUser.getId()
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
                                        ErrorCode
                                                .NOTIFICATION_NOT_FOUND
                                )
                        );

        return toResponse(
                notification,
                recipient
        );
    }

    // ===================== MARK ALL AS READ =====================

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true)
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

    // ===================== DELETE =====================

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.MY_DASHBOARD, allEntries = true)
    public void delete(
            UUID notificationId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        NotificationRecipient recipient =
                getOwnedRecipientOrThrow(
                        notificationId,
                        currentUser.getId()
                );

        recipient.markDeleted(
                CurrentUser.username()
        );

        notificationRecipientRepository.save(
                recipient
        );
    }

    // ===================== HELPER =====================

    private NotificationRecipient
    getOwnedRecipientOrThrow(
            UUID notificationId,
            UUID userId
    ) {
        return notificationRecipientRepository
                .findByNotificationIdAndUserId(
                        notificationId,
                        userId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode
                                        .NOTIFICATION_NOT_FOUND
                        )
                );
    }

    // ===================== MAPPER =====================

    private NotificationResponse toResponse(
            NotificationView view
    ) {
        String targetUrl =
                view.getTargetUrl() != null
                        ? view.getTargetUrl()
                        : notificationTargetUrlResolver.resolve(
                        view.getProjectId(),
                        view.getEntityType(),
                        view.getEntityId()
                );

        return new NotificationResponse(
                view.getId(),
                view.getType(),
                view.getTitle(),
                view.getContent(),
                view.getActorUserId(),
                view.getProjectId(),
                view.getEntityType(),
                view.getEntityId(),
                targetUrl,
                view.getCreatedAt(),
                view.getDeliveredAt(),
                view.getReadAt(),
                view.getReadAt() != null
        );
    }

    private NotificationResponse toResponse(
            Notification notification,
            NotificationRecipient recipient
    ) {
        String targetUrl =
                notification.getTargetUrl() != null
                        ? notification.getTargetUrl()
                        : notificationTargetUrlResolver.resolve(
                        notification.getProjectId(),
                        notification.getEntityType(),
                        notification.getEntityId()
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
                targetUrl,
                notification.getCreatedAt(),
                recipient.getDeliveredAt(),
                recipient.getReadAt(),
                recipient.getReadAt() != null
        );
    }
}
