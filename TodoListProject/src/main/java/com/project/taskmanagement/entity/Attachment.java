package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.AttachmentEntityType;
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

import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attachment extends BaseAuditEntity {

    @Column(name = "project_id", nullable = false)
    UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    AttachmentEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    UUID entityId;

    @Column(name = "uploaded_by_user_id", nullable = false)
    UUID uploadedByUserId;

    @Column(name = "original_file_name", nullable = false, length = 255)
    String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    String storedFileName;

    @Column(name = "content_type", length = 255)
    String contentType;

    @Column(name = "extension", length = 50)
    String extension;

    @Column(name = "size_bytes", nullable = false)
    Long sizeBytes;

    @Column(name = "storage_path", nullable = false, length = 1000)
    String storagePath;
}
