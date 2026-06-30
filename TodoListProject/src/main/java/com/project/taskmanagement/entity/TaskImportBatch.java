package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.TaskImportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_import_batches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class TaskImportBatch extends BaseAuditEntity {

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    @Column(
            name = "sprint_id",
            nullable = false
    )
    UUID sprintId;

    @Column(
            name = "imported_by_user_id",
            nullable = false
    )
    UUID importedByUserId;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 500
    )
    String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    TaskImportStatus status;

    @Builder.Default
    @Column(
            name = "total_rows",
            nullable = false
    )
    Integer totalRows = 0;

    @Builder.Default
    @Column(
            name = "success_rows",
            nullable = false
    )
    Integer successRows = 0;

    @Builder.Default
    @Column(
            name = "failed_rows",
            nullable = false
    )
    Integer failedRows = 0;

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "completed_at")
    Instant completedAt;

    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    String errorMessage;
}