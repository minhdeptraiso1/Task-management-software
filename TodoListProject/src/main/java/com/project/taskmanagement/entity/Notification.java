package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE
)
public class Notification
        extends BaseIdEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 100
    )
    NotificationType type;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    String title;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    String content;

    @Column(name = "actor_user_id")
    UUID actorUserId;

    @Column(name = "project_id")
    UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entity_type",
            length = 50
    )
    ActivityEntityType entityType;

    @Column(name = "entity_id")
    UUID entityId;

    @Column(
            name = "dedup_key",
            length = 255,
            unique = true
    )
    String dedupKey;

    @Column(
            name = "target_url",
            length = 500
    )
    String targetUrl;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    Instant createdAt;
}
