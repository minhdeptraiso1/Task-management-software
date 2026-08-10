package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.projection.report.ReportMemberPerformanceExcelView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository
        extends JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
            ORDER BY t.position ASC,
                     t.createdAt ASC,
                     t.id ASC
            """)
    List<Task> lockSprintColumnTasks(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("status") TaskStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId IS NULL
              AND t.status = :status
            ORDER BY t.position ASC,
                     t.createdAt ASC,
                     t.id ASC
            """)
    List<Task> lockBacklogColumnTasks(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status
    );

    Optional<Task> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    List<Task>
    findAllByProjectIdAndCurrentSprintIdOrderByStatusAscPositionAsc(
            UUID projectId,
            UUID currentSprintId
    );

    List<Task>
    findAllByProjectIdAndCurrentSprintIdAndStatusOrderByPositionAsc(
            UUID projectId,
            UUID currentSprintId,
            TaskStatus status
    );

    long countByProjectIdAndCurrentSprintId(
            UUID projectId,
            UUID currentSprintId
    );

    long countByProjectIdAndCurrentSprintIdAndStatus(
            UUID projectId,
            UUID currentSprintId,
            TaskStatus status
    );

    @Query("""
            SELECT COALESCE(MAX(t.position), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND (
                    (:sprintId IS NULL AND t.currentSprintId IS NULL)
                    OR t.currentSprintId = :sprintId
                  )
              AND t.status = :status
            """)
    Long findMaxPosition(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE Task t
            SET t.position = t.position - 1
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
              AND t.position > :removedPosition
            """)
    int shiftPositionsDown(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status,

            @Param("removedPosition")
            Long removedPosition
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE Task t
            SET t.position = t.position + 1
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
              AND t.position >= :fromPosition
            """)
    int shiftPositionsUp(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status,

            @Param("fromPosition")
            Long fromPosition
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE Task t
            SET t.position = t.position + 1
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
              AND t.id <> :taskId
              AND t.position >= :newPosition
              AND t.position < :oldPosition
            """)
    int moveRangeDown(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status,

            @Param("taskId")
            UUID taskId,

            @Param("newPosition")
            Long newPosition,

            @Param("oldPosition")
            Long oldPosition
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE Task t
            SET t.position = t.position - 1
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
              AND t.id <> :taskId
              AND t.position > :oldPosition
              AND t.position <= :newPosition
            """)
    int moveRangeUp(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status,

            @Param("taskId")
            UUID taskId,

            @Param("oldPosition")
            Long oldPosition,

            @Param("newPosition")
            Long newPosition
    );


    @Query("""
            SELECT COALESCE(MAX(t.position), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status = :status
            """)
    Long findMaxSprintPosition(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("status")
            TaskStatus status
    );

    List<Task>
    findAllByProjectIdAndCurrentSprintId(
            UUID projectId,
            UUID currentSprintId
    );

    List<Task>
    findAllByProjectIdAndBacklogItemIdAndStatusNotInOrderByPositionAsc(
            UUID projectId,
            UUID backlogItemId,
            List<TaskStatus> excludedStatuses
    );

    List<Task>
    findAllByProjectIdAndCurrentSprintIdOrderByPositionAsc(
            UUID projectId,
            UUID currentSprintId
    );

    List<Task>
    findAllByProjectIdAndCurrentSprintIdAndStatusNotInOrderByPositionAsc(
            UUID projectId,
            UUID currentSprintId,
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT COALESCE(MAX(t.position), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId IS NULL
              AND t.status = :status
            """)
    Long findMaxBacklogPosition(
            @Param("projectId")
            UUID projectId,

            @Param("status")
            TaskStatus status
    );

    long countByAssigneeUserId(
            UUID assigneeUserId
    );

    @Query("""
            SELECT t.status,
                   COUNT(t)
            FROM Task t
            WHERE t.assigneeUserId = :assigneeUserId
            GROUP BY t.status
            """)
    List<Object[]> countGroupedByStatusAndAssignee(
            @Param("assigneeUserId")
            UUID assigneeUserId
    );

    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.assigneeUserId = :assigneeUserId
              AND t.dueDate < :today
              AND t.status NOT IN :excludedStatuses
            """)
    long countOverdueByAssignee(
            @Param("assigneeUserId")
            UUID assigneeUserId,

            @Param("today")
            java.time.LocalDate today,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.assigneeUserId = :assigneeUserId
              AND t.dueDate BETWEEN :fromDate AND :toDate
              AND t.status NOT IN :excludedStatuses
            """)
    long countDueSoonByAssignee(
            @Param("assigneeUserId")
            UUID assigneeUserId,

            @Param("fromDate")
            java.time.LocalDate fromDate,

            @Param("toDate")
            java.time.LocalDate toDate,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.dueDate BETWEEN :fromDate AND :toDate
              AND t.status NOT IN :excludedStatuses
              AND t.assigneeUserId IS NOT NULL
            ORDER BY t.dueDate ASC,
                     t.priority DESC,
                     t.createdAt ASC
            """)
    List<Task> findDueSoonTasks(
            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.dueDate < :today
              AND t.status NOT IN :excludedStatuses
            ORDER BY t.dueDate ASC,
                     t.priority DESC,
                     t.createdAt ASC
            """)
    List<Task> findOverdueTasks(
            @Param("today")
            LocalDate today,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.assigneeUserId = :userId
              AND t.status NOT IN :excludedStatuses
              AND (
                    t.dueDate <= :dueSoonToDate
                    OR t.status = com.project.taskmanagement.enums.TaskStatus.BLOCKED
              )
            ORDER BY t.dueDate ASC,
                     t.priority DESC,
                     t.createdAt DESC
            """)
    List<Task> findTasksForDailyDigest(
            @Param("userId")
            UUID userId,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses,

            @Param("dueSoonToDate")
            LocalDate dueSoonToDate
    );

    List<Task> findAllByProjectId(
            UUID projectId
    );

    Page<Task> findAllByProjectId(
            UUID projectId,
            Pageable pageable
    );

    long countByProjectId(
            UUID projectId
    );

    long countByProjectIdAndStatus(
            UUID projectId,
            TaskStatus status
    );

    long countByStatus(
            TaskStatus status
    );

    long countByDueDateBeforeAndStatusNotIn(
            LocalDate today,
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.dueDate < :today
              AND t.status NOT IN :excludedStatuses
            """)
    long countOverdueByProjectId(
            @Param("projectId")
            UUID projectId,

            @Param("today")
            java.time.LocalDate today,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT COALESCE(SUM(t.estimatedMinutes), 0)
            FROM Task t
            WHERE t.projectId = :projectId
            """)
    Long sumEstimatedMinutesByProjectId(
            @Param("projectId")
            UUID projectId
    );

    @Query("""
            SELECT COALESCE(SUM(t.estimatedMinutes), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND (
                    :taskId IS NULL
                    OR t.id = :taskId
              )
              AND (
                    :userId IS NULL
                    OR t.assigneeUserId = :userId
              )
            """)
    Long sumEstimatedMinutesForReport(
            @Param("projectId")
            UUID projectId,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT t.assigneeUserId,
                   COALESCE(SUM(t.estimatedMinutes), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.assigneeUserId IS NOT NULL
            GROUP BY t.assigneeUserId
            """)
    List<Object[]> sumEstimatedMinutesGroupedByAssigneeInSprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId
    );

    @Query("""
            SELECT t.assigneeUserId,
                   t.status,
                   COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.assigneeUserId IS NOT NULL
            GROUP BY t.assigneeUserId, t.status
            """)
    List<Object[]> countTasksGroupedByAssigneeAndStatusInSprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId
    );

    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.assigneeUserId IS NULL
              AND t.status NOT IN :excludedStatuses
            """)
    long countUnassignedActiveTasksInSprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.estimatedMinutes IS NULL
              AND t.status NOT IN :excludedStatuses
            """)
    long countNoEstimateActiveTasksInSprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status NOT IN :excludedStatuses
            ORDER BY t.dueDate ASC,
                     t.position ASC
            """)
    List<Task> findUnfinishedBySprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.dueDate < :today
              AND t.status NOT IN :excludedStatuses
            ORDER BY t.dueDate ASC,
                     t.position ASC
            """)
    List<Task> findOverdueBySprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("today")
            java.time.LocalDate today,

            @Param("excludedStatuses")
            List<TaskStatus> excludedStatuses
    );

    @Query("""
            SELECT u.id AS userId,
                   u.username AS username,
                   u.email AS email,
                   COUNT(DISTINCT t.id) AS totalTasks,
                   COUNT(DISTINCT CASE
                       WHEN t.status = com.project.taskmanagement.enums.TaskStatus.DONE
                       THEN t.id
                       ELSE NULL
                   END) AS doneTasks,
                   COUNT(DISTINCT CASE
                       WHEN t.status NOT IN (
                           com.project.taskmanagement.enums.TaskStatus.DONE,
                           com.project.taskmanagement.enums.TaskStatus.CANCELLED
                       )
                       THEN t.id
                       ELSE NULL
                   END) AS activeTasks,
                   COUNT(DISTINCT CASE
                       WHEN t.status = com.project.taskmanagement.enums.TaskStatus.BLOCKED
                       THEN t.id
                       ELSE NULL
                   END) AS blockedTasks,
                   COUNT(DISTINCT CASE
                       WHEN t.dueDate < :today
                        AND t.status NOT IN (
                            com.project.taskmanagement.enums.TaskStatus.DONE,
                            com.project.taskmanagement.enums.TaskStatus.CANCELLED
                        )
                       THEN t.id
                       ELSE NULL
                   END) AS overdueTasks,
                   COALESCE(SUM(DISTINCT t.estimatedMinutes), 0) AS estimatedMinutes,
                   (SELECT COALESCE(SUM(tl2.minutes), 0)
                    FROM TaskTimeLog tl2
                    JOIN Task t2
                        ON t2.id = tl2.taskId
                    WHERE tl2.userId = u.id
                      AND t2.projectId = :projectId
                      AND (
                            CAST(:sprintId AS java.util.UUID) IS NULL
                            OR t2.currentSprintId = :sprintId
                            OR t2.originSprintId = :sprintId
                      )
                      AND (CAST(:fromDate AS java.time.LocalDate) IS NULL OR tl2.workDate >= :fromDate)
                      AND (CAST(:toDate AS java.time.LocalDate) IS NULL OR tl2.workDate <= :toDate)
                   ) AS spentMinutes
            FROM ProjectMember pm
            JOIN User u
                ON u.id = pm.userId
            LEFT JOIN Task t
                ON (t.assigneeUserId = u.id OR t.reporterUserId = u.id)
               AND t.projectId = pm.projectId
               AND (
                    CAST(:sprintId AS java.util.UUID) IS NULL
                    OR t.currentSprintId = :sprintId
                    OR t.originSprintId = :sprintId
               )
            WHERE pm.projectId = :projectId
              AND (
                    CAST(:userId AS java.util.UUID) IS NULL
                    OR u.id = :userId
              )
            GROUP BY u.id,
                     u.username,
                     u.email
            ORDER BY u.username ASC
            """)
    List<ReportMemberPerformanceExcelView> findMemberPerformanceForExcelReport(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("today") LocalDate today
    );

}
