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
@Table(name = "task_comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class TaskComment extends BaseAuditEntity {

    @Column(
            name = "task_id",
            nullable = false
    )
    UUID taskId;

    @Column(
            name = "user_id",
            nullable = false
    )
    UUID userId;

    /**
     * Null nếu đây là comment gốc.
     * Có giá trị nếu đây là reply.
     */
    @Column(name = "parent_comment_id")
    UUID parentCommentId;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    String content;

    @Column(name = "edited_at")
    Instant editedAt;
}