package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "task_time_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class TaskTimeLog extends BaseAuditEntity {

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

    @Column(
            name = "work_date",
            nullable = false
    )
    LocalDate workDate;

    @Column(
            name = "minutes",
            nullable = false
    )
    Integer minutes;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    String description;
}