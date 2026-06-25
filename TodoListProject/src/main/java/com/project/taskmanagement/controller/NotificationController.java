package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.notification.NotificationPageResponse;
import com.project.taskmanagement.dto.response.notification.NotificationResponse;
import com.project.taskmanagement.dto.response.notification.UnreadNotificationCountResponse;
import com.project.taskmanagement.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class NotificationController {

    NotificationQueryService
            notificationQueryService;

    // ===================== LIST =====================

    @Operation(
            summary = "Lấy danh sách thông báo của người dùng"
    )
    @GetMapping
    public ApiResponseSever<NotificationPageResponse>
    getMyNotifications(
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                notificationQueryService
                        .getMyNotifications(
                                pageable
                        )
        );
    }

    // ===================== UNREAD COUNT =====================

    @Operation(
            summary = "Lấy số thông báo chưa đọc"
    )
    @GetMapping("/unread-count")
    public ApiResponseSever<
            UnreadNotificationCountResponse
            > getUnreadCount() {

        return ApiResponseSever.ok(
                notificationQueryService
                        .getUnreadCount()
        );
    }

    // ===================== MARK ONE AS READ =====================

    @Operation(
            summary = "Đánh dấu một thông báo là đã đọc"
    )
    @PatchMapping("/{notificationId}/read")
    public ApiResponseSever<NotificationResponse>
    markAsRead(
            @PathVariable
            UUID notificationId
    ) {
        return ApiResponseSever.ok(
                notificationQueryService
                        .markAsRead(
                                notificationId
                        )
        );
    }

    // ===================== MARK ALL AS READ =====================

    @Operation(
            summary = "Đánh dấu tất cả thông báo là đã đọc"
    )
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationQueryService
                .markAllAsRead();

        return ResponseEntity
                .noContent()
                .build();
    }
}