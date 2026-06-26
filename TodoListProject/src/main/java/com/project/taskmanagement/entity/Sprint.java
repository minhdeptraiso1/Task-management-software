package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.SprintStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sprints")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class Sprint extends BaseAuditEntity {

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    String name;

    @Column(
            name = "goal",
            columnDefinition = "TEXT"
    )
    String goal;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    SprintStatus status =
            SprintStatus.PLANNING;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "completed_at")
    Instant completedAt;

    @Column(
            name = "created_by_user_id",
            nullable = false
    )
    UUID createdByUserId;
}