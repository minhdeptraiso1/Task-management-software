package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.repository.NotificationRecipientRepository;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.model.NotificationCommand;
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
    NotificationRecipientRepository
            notificationRecipientRepository;

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

        /*
         * Loại bỏ userId trùng nhau nhưng vẫn giữ
         * đúng thứ tự ban đầu.
         */
        LinkedHashSet<UUID> uniqueRecipients =
                new LinkedHashSet<>(
                        command.recipientUserIds()
                );

        /*
         * Loại bỏ recipient null nếu dữ liệu đầu vào
         * có chứa phần tử null.
         */
        uniqueRecipients.remove(null);

        /*
         * Không gửi thông báo cho chính người
         * thực hiện hành động.
         */
        if (command.actorUserId() != null) {
            uniqueRecipients.remove(
                    command.actorUserId()
            );
        }

        if (uniqueRecipients.isEmpty()) {
            return;
        }

        Notification notification =
                Notification.builder()
                        .type(command.type())
                        .title(command.title())
                        .content(command.content())
                        .actorUserId(
                                command.actorUserId()
                        )
                        .projectId(
                                command.projectId()
                        )
                        .entityType(
                                command.entityType()
                        )
                        .entityId(
                                command.entityId()
                        )
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

        notificationRecipientRepository
                .saveAll(recipients);

        /*
         * Sau này gọi WebSocket tại đây.
         *
         * Việc gửi WebSocket nên được xử lý riêng
         * và không làm rollback giao dịch chính
         * nếu kết nối real-time thất bại.
         */
    }
}
