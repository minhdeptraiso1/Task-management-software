package com.project.taskmanagement.dto.request.notification;

import com.project.taskmanagement.enums.NotificationType;

public record NotificationSearchRequest(

        NotificationType type,

        Boolean unread

) {
}