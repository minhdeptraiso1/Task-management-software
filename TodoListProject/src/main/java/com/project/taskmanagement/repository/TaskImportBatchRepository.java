package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.enums.TaskImportStatus;
import com.project.taskmanagement.repository.projection.admin.AdminImportAuditView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TaskImportBatchRepository
        extends JpaRepository<TaskImportBatch, UUID>,
        JpaSpecificationExecutor<TaskImportBatch> {

    Optional<TaskImportBatch> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    Page<TaskImportBatch>
    findAllByProjectIdOrderByCreatedAtDesc(
            UUID projectId,
            Pageable pageable
    );

    long countByProjectIdAndStatus(
            UUID projectId,
            TaskImportStatus status
    );

    @Query(
            value = """
                    SELECT
                        b.id AS batchId,
                        p.id AS projectId,
                        p.code AS projectCode,
                        p.name AS projectName,
                        s.id AS sprintId,
                        s.name AS sprintName,
                        u.id AS importedByUserId,
                        u.username AS importedByUsername,
                        b.originalFileName AS fileName,
                        b.status AS status,
                        b.totalRows AS totalRows,
                        b.successRows AS successRows,
                        b.failedRows AS failedRows,
                        b.startedAt AS startedAt,
                        b.completedAt AS completedAt,
                        b.createdAt AS createdAt
                    FROM TaskImportBatch b
                    JOIN Project p ON p.id = b.projectId
                    LEFT JOIN Sprint s ON s.id = b.sprintId
                    LEFT JOIN User u ON u.id = b.importedByUserId
                    WHERE (:projectId IS NULL OR b.projectId = :projectId)
                      AND (:importedByUserId IS NULL OR b.importedByUserId = :importedByUserId)
                      AND (:status IS NULL OR b.status = :status)
                      AND (:from IS NULL OR b.createdAt >= :from)
                      AND (:to IS NULL OR b.createdAt <= :to)
                      AND (
                            :keyword IS NULL
                            OR LOWER(b.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    """,
            countQuery = """
                    SELECT COUNT(b)
                    FROM TaskImportBatch b
                    JOIN Project p ON p.id = b.projectId
                    LEFT JOIN User u ON u.id = b.importedByUserId
                    WHERE (:projectId IS NULL OR b.projectId = :projectId)
                      AND (:importedByUserId IS NULL OR b.importedByUserId = :importedByUserId)
                      AND (:status IS NULL OR b.status = :status)
                      AND (:from IS NULL OR b.createdAt >= :from)
                      AND (:to IS NULL OR b.createdAt <= :to)
                      AND (
                            :keyword IS NULL
                            OR LOWER(b.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    """
    )
    Page<AdminImportAuditView> searchAdminImportAudits(
            @Param("projectId")
            UUID projectId,

            @Param("importedByUserId")
            UUID importedByUserId,

            @Param("status")
            TaskImportStatus status,

            @Param("from")
            Instant from,

            @Param("to")
            Instant to,

            @Param("keyword")
            String keyword,

            Pageable pageable
    );
}
