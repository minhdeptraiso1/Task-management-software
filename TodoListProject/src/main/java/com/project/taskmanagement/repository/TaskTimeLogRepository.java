package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.repository.projection.report.ReportTimeLogExcelView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeDailyView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeMemberView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeTaskView;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetDailySummaryView;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetEntryView;
import com.project.taskmanagement.repository.projection.timesheet.TimesheetUserSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskTimeLogRepository
        extends JpaRepository<TaskTimeLog, UUID> {

    Optional<TaskTimeLog> findByIdAndTaskId(
            UUID id,
            UUID taskId
    );

    Page<TaskTimeLog>
    findAllByTaskIdOrderByWorkDateDescCreatedAtDesc(
            UUID taskId,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.taskId = :taskId
            """)
    Long sumMinutesByTaskId(
            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.userId = :userId
              AND t.workDate = :workDate
            """)
    Long sumMinutesByUserIdAndWorkDate(
            @Param("userId")
            UUID userId,

            @Param("workDate")
            LocalDate workDate
    );

    @Query("""
            SELECT COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.userId = :userId
              AND t.workDate = :workDate
              AND t.id <> :excludeTimeLogId
            """)
    Long sumMinutesByUserIdAndWorkDateExcludingId(
            @Param("userId")
            UUID userId,

            @Param("workDate")
            LocalDate workDate,

            @Param("excludeTimeLogId")
            UUID excludeTimeLogId
    );

    @Query("""
            SELECT t.userId, COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.taskId = :taskId
            GROUP BY t.userId
            ORDER BY SUM(t.minutes) DESC
            """)
    List<Object[]> sumMinutesGroupedByUser(
            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.userId = :userId
              AND t.workDate BETWEEN :fromDate AND :toDate
            """)
    Long sumMinutesByUserAndDateRange(
            @Param("userId")
            UUID userId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate
    );

    @Query("""
            SELECT t.taskId,
                   COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.taskId IN :taskIds
            GROUP BY t.taskId
            """)
    List<Object[]> sumMinutesGroupedByTaskIds(
            @Param("taskIds")
            List<UUID> taskIds
    );

    @Query("""
            SELECT COALESCE(SUM(tl.minutes), 0)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
            """)
    Long sumProjectMinutes(
            @Param("projectId")
            UUID projectId
    );

    @Query("""
            SELECT COALESCE(SUM(tl.minutes), 0)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            """)
    Long sumProjectMinutes(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT COUNT(tl)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            """)
    long countProjectTimeLogs(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT COUNT(DISTINCT tl.taskId)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            """)
    long countDistinctTasksWithTimeLog(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT COUNT(DISTINCT tl.userId)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            """)
    long countDistinctContributors(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT
                tl.workDate AS workDate,
                COALESCE(SUM(tl.minutes), 0) AS spentMinutes,
                COUNT(tl) AS logCount
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            GROUP BY tl.workDate
            ORDER BY tl.workDate ASC
            """)
    List<ProjectTimeDailyView> summarizeProjectTimeByDate(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT
                tl.userId AS userId,
                COALESCE(SUM(tl.minutes), 0) AS spentMinutes,
                COUNT(tl) AS logCount,
                COUNT(DISTINCT tl.taskId) AS taskCount,
                MIN(tl.workDate) AS firstWorkDate,
                MAX(tl.workDate) AS lastWorkDate
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            GROUP BY tl.userId
            ORDER BY SUM(tl.minutes) DESC
            """)
    List<ProjectTimeMemberView> summarizeProjectTimeByMember(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT
                tl.taskId AS taskId,
                COALESCE(SUM(tl.minutes), 0) AS spentMinutes,
                COUNT(tl) AS logCount,
                COUNT(DISTINCT tl.userId) AS contributorCount
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND tl.workDate BETWEEN :fromDate AND :toDate
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
              )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
              )
            GROUP BY tl.taskId
            ORDER BY SUM(tl.minutes) DESC
            """)
    List<ProjectTimeTaskView> summarizeProjectTimeByTask(
            @Param("projectId")
            UUID projectId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT tl.userId,
                   COALESCE(SUM(tl.minutes), 0)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE t.projectId = :projectId
              AND t.currentSprintId = :sprintId
            GROUP BY tl.userId
            """)
    List<Object[]> sumMinutesGroupedByUserInSprint(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId
    );

    @Query("""
            SELECT
                tl.id AS id,
                p.id AS projectId,
                p.code AS projectCode,
                p.name AS projectName,
                t.id AS taskId,
                t.title AS taskTitle,
                u.id AS userId,
                u.username AS username,
                u.email AS email,
                tl.workDate AS workDate,
                tl.minutes AS minutes,
                tl.description AS description,
                tl.createdAt AS createdAt,
                tl.updatedAt AS updatedAt
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            JOIN Project p
                ON p.id = t.projectId
            JOIN User u
                ON u.id = tl.userId
            WHERE (
                    :projectId IS NULL
                    OR p.id = :projectId
            )
              AND (
                    :userId IS NULL
                    OR u.id = :userId
            )
              AND (
                    :taskId IS NULL
                    OR t.id = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            ORDER BY tl.workDate DESC,
                     tl.createdAt DESC
            """)
    Page<TimesheetEntryView> searchTimesheetEntries(
            @Param("projectId")
            UUID projectId,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            Pageable pageable
    );

    @Query("""
            SELECT
                tl.workDate AS workDate,
                COALESCE(SUM(tl.minutes), 0) AS totalMinutes,
                COUNT(tl) AS logCount
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            GROUP BY tl.workDate
            ORDER BY tl.workDate ASC
            """)
    List<TimesheetDailySummaryView> summarizeTimesheetByDate(
            @Param("projectId")
            UUID projectId,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate
    );

    @Query("""
            SELECT
                u.id AS userId,
                u.username AS username,
                u.email AS email,
                COALESCE(SUM(tl.minutes), 0) AS totalMinutes,
                COUNT(tl) AS logCount,
                COUNT(DISTINCT tl.taskId) AS taskCount
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            JOIN User u
                ON u.id = tl.userId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            GROUP BY u.id,
                     u.username,
                     u.email
            ORDER BY SUM(tl.minutes) DESC
            """)
    List<TimesheetUserSummaryView> summarizeTimesheetByUser(
            @Param("projectId")
            UUID projectId,

            @Param("userId")
            UUID userId,

            @Param("taskId")
            UUID taskId,

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate
    );

    @Query("""
            SELECT COUNT(DISTINCT tl.taskId)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            """)
    long countTimesheetTasks(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("taskId") UUID taskId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(DISTINCT tl.userId)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            """)
    long countTimesheetUsers(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("taskId") UUID taskId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COALESCE(SUM(tl.minutes), 0)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            """)
    Long sumTimesheetMinutes(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("taskId") UUID taskId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(tl)
            FROM TaskTimeLog tl
            JOIN Task t
                ON t.id = tl.taskId
            WHERE (
                    :projectId IS NULL
                    OR t.projectId = :projectId
            )
              AND (
                    :userId IS NULL
                    OR tl.userId = :userId
            )
              AND (
                    :taskId IS NULL
                    OR tl.taskId = :taskId
            )
              AND tl.workDate BETWEEN :fromDate AND :toDate
            """)
    long countTimesheetLogs(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("taskId") UUID taskId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
            SELECT tl.id AS "timeLogId",
                   t.project_id AS "projectId",
                   p.name AS "projectName",
                   t.id AS "taskId",
                   t.title AS "taskTitle",
                   t.current_sprint_id AS "sprintId",
                   s.name AS "sprintName",
                   t.backlog_item_id AS "backlogItemId",
                   bi.title AS "backlogItemTitle",
                   u.id AS "userId",
                   u.username AS "username",
                   u.email AS "email",
                   tl.work_date AS "workDate",
                   tl.minutes AS "minutes",
                   tl.description AS "description",
                   tl.created_at AS "createdAt"
            FROM task_time_logs tl
            JOIN tasks t
                ON t.id = tl.task_id
               AND t.deleted_at IS NULL
            JOIN projects p
                ON p.id = t.project_id
               AND p.deleted_at IS NULL
            LEFT JOIN sprints s
                ON s.id = t.current_sprint_id
               AND s.deleted_at IS NULL
            LEFT JOIN backlog_items bi
                ON bi.id = t.backlog_item_id
               AND bi.deleted_at IS NULL
            JOIN users u
                ON u.id = tl.user_id
               AND u.deleted_at IS NULL
            WHERE tl.deleted_at IS NULL
              AND (
                    CAST(:projectId AS uuid) IS NULL
                    OR t.project_id = CAST(:projectId AS uuid)
              )
              AND (
                    CAST(:sprintId AS uuid) IS NULL
                    OR t.current_sprint_id = CAST(:sprintId AS uuid)
                    OR t.origin_sprint_id = CAST(:sprintId AS uuid)
              )
              AND (
                    CAST(:userId AS uuid) IS NULL
                    OR tl.user_id = CAST(:userId AS uuid)
              )
              AND (
                    CAST(:fromDate AS date) IS NULL
                    OR tl.work_date >= CAST(:fromDate AS date)
              )
              AND (
                    CAST(:toDate AS date) IS NULL
                    OR tl.work_date <= CAST(:toDate AS date)
              )
            ORDER BY tl.work_date ASC,
                     u.username ASC,
                     t.title ASC,
                     tl.created_at ASC
            """, nativeQuery = true)
    List<ReportTimeLogExcelView> findTimeLogsForExcelReport(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
