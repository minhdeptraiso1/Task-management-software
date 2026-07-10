package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskDependencyRepository
        extends JpaRepository<TaskDependency, UUID> {

    boolean existsByTaskIdAndDependsOnTaskId(
            UUID taskId,
            UUID dependsOnTaskId
    );

    Optional<TaskDependency> findByIdAndTaskId(
            UUID id,
            UUID taskId
    );

    List<TaskDependency> findAllByTaskIdOrderByCreatedAtAsc(
            UUID taskId
    );

    List<TaskDependency> findAllByDependsOnTaskId(
            UUID dependsOnTaskId
    );

    @Query("""
            SELECT td.dependsOnTaskId
            FROM TaskDependency td
            WHERE td.taskId = :taskId
            """)
    List<UUID> findDependsOnTaskIds(
            @Param("taskId")
            UUID taskId
    );

    @Query("""
            SELECT td.taskId
            FROM TaskDependency td
            WHERE td.dependsOnTaskId = :dependsOnTaskId
            """)
    List<UUID> findBlockedTaskIdsByDependency(
            @Param("dependsOnTaskId")
            UUID dependsOnTaskId
    );
}
