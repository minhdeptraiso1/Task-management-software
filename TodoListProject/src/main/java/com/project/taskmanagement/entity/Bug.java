package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bugs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bug extends BaseAuditEntity {

    @Column(name = "project_id", nullable = false)
    UUID projectId;

    @Column(name = "backlog_item_id")
    UUID backlogItemId;

    @Column(name = "task_id")
    UUID taskId;

    @Column(name = "sprint_id")
    UUID sprintId;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    BugSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    BugStatus status;

    @Column(name = "assignee_user_id")
    UUID assigneeUserId;

    @Column(name = "reporter_user_id", nullable = false)
    UUID reporterUserId;

    @Column(name = "reproduction_steps", columnDefinition = "TEXT")
    String reproductionSteps;

    @Column(name = "expected_result", columnDefinition = "TEXT")
    String expectedResult;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    String actualResult;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Builder.Default
    @Column(name = "reopened_count", nullable = false)
    Integer reopenedCount = 0;

    @Column(name = "resolved_at")
    Instant resolvedAt;

    @Column(name = "closed_at")
    Instant closedAt;
}
