package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_activity_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectActivityLog extends BaseIdEntity {
    @Column(
            name = "project_id",
            nullable = false)
    UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entity_type",
            nullable = false,
            length = 50)
    ActivityEntityType entityType;

    @Column(name = "entity_id")
    UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 100)
    ProjectActivityAction action;

    @Column(
            name = "performed_by_user_id",
            nullable = false)
    UUID performedByUserId;

    @Column(
            name = "old_value_json",
            columnDefinition = "TEXT")
    String oldValueJson;

    @Column(
            name = "new_value_json",
            columnDefinition = "TEXT")
    String newValueJson;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false)
    Instant createdAt;
}