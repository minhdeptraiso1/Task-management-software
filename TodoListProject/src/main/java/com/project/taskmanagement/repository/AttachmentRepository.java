package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Attachment;
import com.project.taskmanagement.enums.AttachmentEntityType;
import com.project.taskmanagement.repository.projection.attachment.DeletedAttachmentFileView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    Optional<Attachment> findByIdAndProjectId(UUID id, UUID projectId);

    Page<Attachment> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    Page<Attachment> findAllByProjectIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId,
            Pageable pageable
    );

    long countByProjectIdAndEntityTypeAndEntityId(
            UUID projectId,
            AttachmentEntityType entityType,
            UUID entityId
    );

    @Query("""
            SELECT COALESCE(SUM(a.sizeBytes), 0)
            FROM Attachment a
            WHERE a.projectId = :projectId
            """)
    Long sumSizeBytesByProjectId(@Param("projectId") UUID projectId);

    @Query("""
            SELECT COALESCE(SUM(a.sizeBytes), 0)
            FROM Attachment a
            """)
    Long sumTotalSizeBytes();

    @Query(
            value = """
                    SELECT id AS id,
                           storage_path AS storagePath
                    FROM attachments
                    WHERE deleted_at IS NOT NULL
                      AND deleted_at < :deletedBefore
                      AND storage_path IS NOT NULL
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<DeletedAttachmentFileView> findDeletedFilesForCleanup(
            @Param("deletedBefore") Instant deletedBefore,
            @Param("limit") int limit
    );

    @Query(
            value = """
                    SELECT COUNT(1)
                    FROM attachments
                    WHERE storage_path = :storagePath
                    """,
            nativeQuery = true
    )
    long countAllRowsByStoragePath(@Param("storagePath") String storagePath);
}
