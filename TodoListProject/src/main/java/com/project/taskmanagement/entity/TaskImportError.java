package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_import_errors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskImportError extends BaseIdEntity {

    @Column(
            name = "import_batch_id",
            nullable = false
    )
    UUID importBatchId;

    @Column(
            name = "row_number",
            nullable = false
    )
    Integer rowNumber;

    @Column(
            name = "field_name",
            length = 100
    )
    String fieldName;

    @Column(
            name = "raw_value",
            columnDefinition = "TEXT"
    )
    String rawValue;

    @Column(
            name = "error_message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    String errorMessage;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    Instant createdAt;
}