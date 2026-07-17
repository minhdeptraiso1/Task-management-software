package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "bug_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class BugAttachment extends BaseAuditEntity {

    @Column(name = "project_id", nullable = false)
    UUID projectId;

    @Column(name = "bug_id", nullable = false)
    UUID bugId;

    @Column(name = "uploaded_by_user_id", nullable = false)
    UUID uploadedByUserId;

    @Column(name = "original_file_name", nullable = false, length = 500)
    String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 500)
    String storedFileName;

    @Column(name = "content_type", nullable = false)
    String contentType;

    @Column(name = "size_bytes", nullable = false)
    Long sizeBytes;

    @Column(name = "storage_path", nullable = false, length = 1000)
    String storagePath;
}
