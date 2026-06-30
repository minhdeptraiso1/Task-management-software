package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


}