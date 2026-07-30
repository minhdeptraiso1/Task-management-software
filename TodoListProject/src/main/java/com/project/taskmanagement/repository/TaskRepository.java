package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.repository.projection.report.ReportMemberPerformanceExcelView;
import com.project.taskmanagement.repository.projection.taskexport.SprintTaskExportRowView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository
        extends JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    List<Task>
    findAllByProjectIdAndBacklogItemIdOrderByPositionAsc(
            UUID projectId,
            UUID backlogItemId
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

    List<Task>
    findAllByProjectIdAndCurrentSprintIdAndStatusNotIn(
            UUID projectId,
            UUID currentSprintId,
            List<TaskStatus> statuses
    );

    List<Task>
    findAllByProjectIdAndBacklogItemIdAndStatusNotIn(
            UUID projectId,
            UUID backlogItemId,
            List<TaskStatus> statuses
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

    boolean existsByProjectIdAndBacklogItemId(
            UUID projectId,
            UUID backlogItemId
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

    @Query("""
            SELECT t.status,
                   COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
            GROUP BY t.status
            """)
    List<Object[]> countGroupedByStatusAndProjectId(
            @Param("projectId")
            UUID projectId
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
            SELECT COALESCE(SUM(t.estimatedMinutes), 0)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
            """)
    Long sumEstimatedMinutesByProjectIdAndCurrentSprintId(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId
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
            SELECT COUNT(t)
            FROM Task t
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
              AND t.status NOT IN :excludedStatuses
            """)
    long countUnfinishedBySprint(
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
            SELECT t.id AS id,
                   t.title AS title,
                   t.description AS description,
                   t.type AS type,
                   t.status AS status,
                   t.priority AS priority,
                   p.id AS projectId,
                   p.code AS projectCode,
                   p.name AS projectName,
                   s.id AS sprintId,
                   s.name AS sprintName,
                   bi.id AS backlogItemId,
                   bi.title AS backlogItemTitle,
                   assignee.id AS assigneeUserId,
                   assignee.username AS assigneeUsername,
                   assignee.email AS assigneeEmail,
                   reporter.id AS reporterUserId,
                   reporter.username AS reporterUsername,
                   reporter.email AS reporterEmail,
                   t.estimatedMinutes AS estimatedMinutes,
                   COALESCE(SUM(tl.minutes), 0) AS loggedMinutes,
                   t.startDate AS startDate,
                   t.dueDate AS dueDate,
                   t.completedAt AS completedAt,
                   t.position AS position,
                   t.createdAt AS createdAt,
                   t.updatedAt AS updatedAt
            FROM Task t
            JOIN Project p ON p.id = t.projectId
            LEFT JOIN Sprint s ON s.id = t.currentSprintId
            LEFT JOIN BacklogItem bi ON bi.id = t.backlogItemId
            LEFT JOIN User assignee ON assignee.id = t.assigneeUserId
            LEFT JOIN User reporter ON reporter.id = t.reporterUserId
            LEFT JOIN TaskTimeLog tl ON tl.taskId = t.id
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
            GROUP BY t.id, t.title, t.description, t.type, t.status, t.priority,
                     p.id, p.code, p.name, s.id, s.name, bi.id, bi.title,
                     assignee.id, assignee.username, assignee.email,
                     reporter.id, reporter.username, reporter.email,
                     t.estimatedMinutes, t.startDate, t.dueDate, t.completedAt,
                     t.position, t.createdAt, t.updatedAt
            ORDER BY t.status ASC, t.position ASC, t.createdAt DESC
            """)
    List<SprintTaskExportRowView> findSprintTaskExportRows(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId
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
                   COALESCE(SUM(tl.minutes), 0) AS spentMinutes
            FROM ProjectMember pm
            JOIN User u
                ON u.id = pm.userId
            LEFT JOIN Task t
                ON t.assigneeUserId = u.id
               AND t.projectId = pm.projectId
               AND (
                    CAST(:sprintId AS java.util.UUID) IS NULL
                    OR t.currentSprintId = :sprintId
                    OR t.originSprintId = :sprintId
               )
            LEFT JOIN TaskTimeLog tl
                ON tl.taskId = t.id
               AND tl.userId = u.id
               AND (
                    CAST(:fromDate AS java.time.LocalDate) IS NULL
                    OR tl.workDate >= :fromDate
               )
               AND (
                    CAST(:toDate AS java.time.LocalDate) IS NULL
                    OR tl.workDate <= :toDate
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
