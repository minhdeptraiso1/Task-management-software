package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_recipients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE
)
public class NotificationRecipient
        extends BaseIdEntity {

    @Column(
            name = "notification_id",
            nullable = false
    )
    UUID notificationId;

    @Column(
            name = "user_id",
            nullable = false
    )
    UUID userId;

    @Column(name = "delivered_at")
    Instant deliveredAt;

    @Column(name = "read_at")
    Instant readAt;
}