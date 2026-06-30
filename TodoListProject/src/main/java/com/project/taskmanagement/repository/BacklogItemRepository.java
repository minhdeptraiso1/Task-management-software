package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.enums.BacklogItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BacklogItemRepository
        extends JpaRepository<BacklogItem, UUID>,
        JpaSpecificationExecutor<BacklogItem> {

    Optional<BacklogItem> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    List<BacklogItem>
    findAllByProjectIdAndSprintIdIsNullOrderByPositionAsc(
            UUID projectId
    );

    List<BacklogItem>
    findAllByProjectIdAndSprintIdOrderByPositionAsc(
            UUID projectId,
            UUID sprintId
    );

    boolean existsByProjectIdAndSprintIdAndStatusNot(
            UUID projectId,
            UUID sprintId,
            BacklogItemStatus status
    );

    long countByProjectId(
            UUID projectId
    );

    @Query("""
            SELECT COALESCE(MAX(b.position), 0)
            FROM BacklogItem b
            WHERE b.projectId = :projectId
              AND b.sprintId IS NULL
            """)
    Long findMaxBacklogPosition(
            @Param("projectId")
            UUID projectId
    );

    @Query("""
            SELECT COALESCE(MAX(b.position), 0)
            FROM BacklogItem b
            WHERE b.projectId = :projectId
              AND b.sprintId = :sprintId
            """)
    Long findMaxSprintPosition(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId
    );

    long countByProjectIdAndSprintId(
            UUID projectId,
            UUID sprintId
    );

    boolean existsByProjectIdAndSprintId(
            UUID projectId,
            UUID sprintId
    );

    @Modifying
    @Query("""
            UPDATE BacklogItem b
            SET b.position = b.position + 1
            WHERE b.projectId = :projectId
              AND (
                    (:sprintId IS NULL AND b.sprintId IS NULL)
                    OR b.sprintId = :sprintId
                  )
              AND b.position >= :fromPosition
            """)
    int shiftPositionsUp(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("fromPosition")
            Long fromPosition
    );

    @Modifying
    @Query("""
            UPDATE BacklogItem b
            SET b.position = b.position - 1
            WHERE b.projectId = :projectId
              AND (
                    (:sprintId IS NULL AND b.sprintId IS NULL)
                    OR b.sprintId = :sprintId
                  )
              AND b.position > :removedPosition
            """)
    int shiftPositionsDown(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("removedPosition")
            Long removedPosition
    );

    @Modifying
    @Query("""
            UPDATE BacklogItem b
            SET b.position = b.position + 1
            WHERE b.projectId = :projectId
              AND (
                    (:sprintId IS NULL AND b.sprintId IS NULL)
                    OR b.sprintId = :sprintId
                  )
              AND b.id <> :itemId
              AND b.position >= :newPosition
              AND b.position < :oldPosition
            """)
    int moveRangeDown(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("itemId")
            UUID itemId,

            @Param("newPosition")
            Long newPosition,

            @Param("oldPosition")
            Long oldPosition
    );

    @Modifying
    @Query("""
            UPDATE BacklogItem b
            SET b.position = b.position - 1
            WHERE b.projectId = :projectId
              AND (
                    (:sprintId IS NULL AND b.sprintId IS NULL)
                    OR b.sprintId = :sprintId
                  )
              AND b.id <> :itemId
              AND b.position > :oldPosition
              AND b.position <= :newPosition
            """)
    int moveRangeUp(
            @Param("projectId")
            UUID projectId,

            @Param("sprintId")
            UUID sprintId,

            @Param("itemId")
            UUID itemId,

            @Param("oldPosition")
            Long oldPosition,

            @Param("newPosition")
            Long newPosition
    );


}