package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class Task extends BaseAuditEntity {

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    @Column(
            name = "backlog_item_id",
            nullable = false
    )
    UUID backlogItemId;

    /**
     * Sprint hiện tại của Task.
     * <p>
     * Có thể null nếu Backlog Item đang nằm
     * trong Product Backlog.
     */
    @Column(name = "current_sprint_id")
    UUID currentSprintId;

    @Column(name = "origin_sprint_id")
    UUID originSprintId;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    String title;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 30
    )
    TaskType type =
            TaskType.DEVELOPMENT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    TaskStatus status =
            TaskStatus.TODO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            length = 30
    )
    TaskPriority priority =
            TaskPriority.MEDIUM;

    @Column(name = "assignee_user_id")
    UUID assigneeUserId;

    @Column(
            name = "reporter_user_id",
            nullable = false
    )
    UUID reporterUserId;

    @Column(name = "estimated_minutes")
    Integer estimatedMinutes;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Column(name = "completed_at")
    Instant completedAt;

    @Builder.Default
    @Column(
            name = "position",
            nullable = false
    )
    Long position = 1L;
}