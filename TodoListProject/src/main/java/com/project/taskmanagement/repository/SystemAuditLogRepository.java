package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.SystemAuditLog;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.repository.projection.admin.AdminAuditDailySummaryView;
import com.project.taskmanagement.repository.projection.admin.AdminAuditSummaryItemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SystemAuditLogRepository
        extends JpaRepository<SystemAuditLog, UUID>,
        JpaSpecificationExecutor<SystemAuditLog> {

    long countByResourceType(
            SystemAuditResourceType resourceType
    );

    @Query("""
            SELECT
                CAST(s.action AS string) AS name,
                COUNT(s) AS count
            FROM SystemAuditLog s
            WHERE s.createdAt BETWEEN :from AND :to
            GROUP BY s.action
            ORDER BY COUNT(s) DESC
            """)
    List<AdminAuditSummaryItemView> summarizeByAction(
            @Param("from")
            Instant from,

            @Param("to")
            Instant to
    );

    @Query("""
            SELECT
                CAST(s.resourceType AS string) AS name,
                COUNT(s) AS count
            FROM SystemAuditLog s
            WHERE s.createdAt BETWEEN :from AND :to
            GROUP BY s.resourceType
            ORDER BY COUNT(s) DESC
            """)
    List<AdminAuditSummaryItemView> summarizeByResourceType(
            @Param("from")
            Instant from,

            @Param("to")
            Instant to
    );

    @Query(
            value = """
                    SELECT
                        CAST(created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS DATE) AS date,
                        COUNT(*) AS count
                    FROM system_audit_logs
                    WHERE created_at BETWEEN :from AND :to
                    GROUP BY CAST(created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS DATE)
                    ORDER BY CAST(created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS DATE) ASC
                    """,
            nativeQuery = true
    )
    List<AdminAuditDailySummaryView> summarizeByDate(
            @Param("from")
            Instant from,

            @Param("to")
            Instant to
    );
}
