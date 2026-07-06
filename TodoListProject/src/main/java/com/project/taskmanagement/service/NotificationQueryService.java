package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.notification.NotificationSearchRequest;
import com.project.taskmanagement.dto.response.notification.NotificationPageResponse;
import com.project.taskmanagement.dto.response.notification.NotificationResponse;
import com.project.taskmanagement.dto.response.notification.UnreadNotificationCountResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationQueryService {

    NotificationPageResponse getMyNotifications(
            NotificationSearchRequest request,
            Pageable pageable
    );

    UnreadNotificationCountResponse getUnreadCount();

    NotificationResponse markAsRead(
            UUID notificationId
    );

    void markAllAsRead();

    void delete(
            UUID notificationId
    );
}