package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskTimeLog;
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
}