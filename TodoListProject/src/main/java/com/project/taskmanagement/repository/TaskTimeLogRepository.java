package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskTimeLog;
import com.project.taskmanagement.repository.projection.report.ProjectTimeDailyView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeMemberView;
import com.project.taskmanagement.repository.projection.report.ProjectTimeTaskView;
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

    List<TaskTimeLog>
    findAllByTaskIdOrderByWorkDateAscCreatedAtAsc(
            UUID taskId
    );

    Page<TaskTimeLog>
    findAllByUserIdAndWorkDateBetweenOrderByWorkDateDesc(
            UUID userId,
            LocalDate fromDate,
            LocalDate toDate,
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
            WHERE t.taskId = :taskId
              AND t.userId = :userId
            """)
    Long sumMinutesByTaskIdAndUserId(
            @Param("taskId")
            UUID taskId,

            @Param("userId")
            UUID userId
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
            SELECT COALESCE(SUM(t.minutes), 0)
            FROM TaskTimeLog t
            WHERE t.taskId IN :taskIds
            """)
    Long sumMinutesByTaskIds(
            @Param("taskIds")
            List<UUID> taskIds
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
}