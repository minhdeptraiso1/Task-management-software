package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.repository.projection.NotificationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRecipientRepository
        extends JpaRepository<NotificationRecipient, UUID> {

    Optional<NotificationRecipient>
    findByNotificationIdAndUserId(
            UUID notificationId,
            UUID userId
    );

    @Query("""
            SELECT COUNT(nr)
            FROM NotificationRecipient nr
            WHERE nr.userId = :userId
              AND nr.readAt IS NULL
            """)
    long countUnreadByUserId(
            @Param("userId")
            UUID userId
    );

    @Query("""
            SELECT
                n.id AS id,
                n.type AS type,
                n.title AS title,
                n.content AS content,
                n.actorUserId AS actorUserId,
                n.projectId AS projectId,
                n.entityType AS entityType,
                n.entityId AS entityId,
                n.targetUrl AS targetUrl,
                n.createdAt AS createdAt,
                nr.deliveredAt AS deliveredAt,
                nr.readAt AS readAt
            FROM NotificationRecipient nr
            JOIN Notification n
                ON n.id = nr.notificationId
            WHERE nr.userId = :userId
              AND (
                    :type IS NULL
                    OR n.type = :type
              )
              AND (
                    :unread IS NULL
                    OR (
                        :unread = TRUE
                        AND nr.readAt IS NULL
                    )
                    OR (
                        :unread = FALSE
                        AND nr.readAt IS NOT NULL
                    )
              )
            ORDER BY n.createdAt DESC
            """)
    Page<NotificationView> searchNotificationViews(
            @Param("userId")
            UUID userId,

            @Param("type")
            NotificationType type,

            @Param("unread")
            Boolean unread,

            Pageable pageable
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE NotificationRecipient nr
            SET nr.readAt = :readAt
            WHERE nr.userId = :userId
              AND nr.readAt IS NULL
            """)
    int markAllAsRead(
            @Param("userId")
            UUID userId,

            @Param("readAt")
            Instant readAt
    );
}
