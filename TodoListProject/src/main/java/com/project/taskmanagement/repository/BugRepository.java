package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.repository.projection.bugreport.BugAssigneeSummaryView;
import com.project.taskmanagement.repository.projection.bugreport.BugPriorityCountView;
import com.project.taskmanagement.repository.projection.bugreport.BugSeverityCountView;
import com.project.taskmanagement.repository.projection.bugreport.BugStatusCountView;
import com.project.taskmanagement.repository.projection.bug.BugExportRowView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BugRepository
        extends JpaRepository<Bug, UUID>,
        JpaSpecificationExecutor<Bug> {

    Optional<Bug> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    long countByProjectId(
            UUID projectId
    );

    long countByProjectIdAndStatus(
            UUID projectId,
            BugStatus status
    );

    long countByProjectIdAndSeverity(
            UUID projectId,
            BugSeverity severity
    );

    long countByStatus(
            BugStatus status
    );

    long countBySeverity(
            BugSeverity severity
    );

    long countByProjectIdAndAssigneeUserIdIsNull(
            UUID projectId
    );

    long countByProjectIdAndAssigneeUserIdIsNotNull(
            UUID projectId
    );

    @Query("""
            SELECT COUNT(b)
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            """)
    long countForReport(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT COUNT(b)
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND b.status = :status
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            """)
    long countForReportByStatus(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT COUNT(b)
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND b.dueDate IS NOT NULL
              AND b.dueDate < :today
              AND b.status NOT IN :doneStatuses
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            """)
    long countOverdueForReport(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT COALESCE(SUM(b.reopenedCount), 0)
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            """)
    Long sumReopenedCountForReport(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT b.status AS status, COUNT(b) AS total
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            GROUP BY b.status
            """)
    List<BugStatusCountView> countGroupedByStatus(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT b.severity AS severity, COUNT(b) AS total
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            GROUP BY b.severity
            """)
    List<BugSeverityCountView> countGroupedBySeverity(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT b.priority AS priority, COUNT(b) AS total
            FROM Bug b
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            GROUP BY b.priority
            """)
    List<BugPriorityCountView> countGroupedByPriority(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT u.id AS assigneeUserId,
                   u.username AS username,
                   u.email AS email,
                   COUNT(b) AS totalBugs,
                   SUM(CASE WHEN b.status = com.project.taskmanagement.enums.BugStatus.OPEN THEN 1 ELSE 0 END) AS openBugs,
                   SUM(CASE WHEN b.status = com.project.taskmanagement.enums.BugStatus.RESOLVED THEN 1 ELSE 0 END) AS resolvedBugs,
                   SUM(CASE WHEN b.status = com.project.taskmanagement.enums.BugStatus.CLOSED THEN 1 ELSE 0 END) AS closedBugs,
                   SUM(CASE WHEN b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses THEN 1 ELSE 0 END) AS overdueBugs,
                   SUM(CASE WHEN b.severity = com.project.taskmanagement.enums.BugSeverity.CRITICAL THEN 1 ELSE 0 END) AS criticalBugs,
                   COALESCE(SUM(b.reopenedCount), 0) AS reopenedBugs
            FROM Bug b
            LEFT JOIN User u ON u.id = b.assigneeUserId
            WHERE b.projectId = :projectId
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
              AND (:overdueOnly = FALSE OR (b.dueDate IS NOT NULL AND b.dueDate < :today AND b.status NOT IN :doneStatuses))
              AND b.createdAt >= :fromDateStart
              AND b.createdAt < :toDateExclusive
            GROUP BY u.id, u.username, u.email
            ORDER BY COUNT(b) DESC
            """)
    List<BugAssigneeSummaryView> summarizeByAssignee(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority,
            @Param("overdueOnly") boolean overdueOnly,
            @Param("today") LocalDate today,
            @Param("doneStatuses") List<BugStatus> doneStatuses,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateExclusive") Instant toDateExclusive
    );

    @Query("""
            SELECT b.id AS id,
                   b.title AS title,
                   b.description AS description,
                   b.status AS status,
                   b.severity AS severity,
                   b.priority AS priority,
                   p.id AS projectId,
                   p.code AS projectCode,
                   p.name AS projectName,
                   s.id AS sprintId,
                   s.name AS sprintName,
                   t.id AS taskId,
                   t.title AS taskTitle,
                   bi.id AS backlogItemId,
                   bi.title AS backlogItemTitle,
                   assignee.id AS assigneeUserId,
                   assignee.username AS assigneeUsername,
                   assignee.email AS assigneeEmail,
                   reporter.id AS reporterUserId,
                   reporter.username AS reporterUsername,
                   reporter.email AS reporterEmail,
                   b.dueDate AS dueDate,
                   b.reopenedCount AS reopenedCount,
                   b.resolvedAt AS resolvedAt,
                   b.closedAt AS closedAt,
                   b.createdAt AS createdAt,
                   b.updatedAt AS updatedAt
            FROM Bug b
            JOIN Project p ON p.id = b.projectId
            LEFT JOIN Sprint s ON s.id = b.sprintId
            LEFT JOIN Task t ON t.id = b.taskId
            LEFT JOIN BacklogItem bi ON bi.id = b.backlogItemId
            LEFT JOIN User assignee ON assignee.id = b.assigneeUserId
            LEFT JOIN User reporter ON reporter.id = b.reporterUserId
            WHERE b.projectId = :projectId
              AND b.createdAt >= :fromInstant
              AND b.createdAt < :toInstantExclusive
              AND (:sprintId IS NULL OR b.sprintId = :sprintId)
              AND (:assigneeUserId IS NULL OR b.assigneeUserId = :assigneeUserId)
              AND (:status IS NULL OR b.status = :status)
              AND (:severity IS NULL OR b.severity = :severity)
              AND (:priority IS NULL OR b.priority = :priority)
            ORDER BY b.createdAt DESC
            """)
    List<BugExportRowView> findBugExportRows(
            @Param("projectId") UUID projectId,
            @Param("fromInstant") Instant fromInstant,
            @Param("toInstantExclusive") Instant toInstantExclusive,
            @Param("sprintId") UUID sprintId,
            @Param("assigneeUserId") UUID assigneeUserId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("priority") TaskPriority priority
    );
}
