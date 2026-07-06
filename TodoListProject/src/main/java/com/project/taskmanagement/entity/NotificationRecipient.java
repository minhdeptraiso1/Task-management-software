package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

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
@SQLRestriction("deleted_at IS NULL")
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

    @Column(name = "deleted_at")
    Instant deletedAt;

    @Column(
            name = "deleted_by",
            length = 100
    )
    String deletedBy;

    // ===================== SOFT DELETE =====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted(
            String deletedBy
    ) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}