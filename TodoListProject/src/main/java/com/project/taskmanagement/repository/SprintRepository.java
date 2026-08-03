package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository
        extends JpaRepository<Sprint, UUID>,
        JpaSpecificationExecutor<Sprint> {

    Optional<Sprint> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Sprint s
            WHERE s.id = :id
              AND s.projectId = :projectId
            """)
    Optional<Sprint> findWithLockByIdAndProjectId(
            @Param("id") UUID id,
            @Param("projectId") UUID projectId
    );

    List<Sprint> findAllByProjectIdOrderByCreatedAtDesc(
            UUID projectId
    );

    boolean existsByProjectIdAndNameIgnoreCase(
            UUID projectId,
            String name
    );

    boolean existsByProjectIdAndNameIgnoreCaseAndIdNot(
            UUID projectId,
            String name,
            UUID id
    );

    boolean existsByProjectIdAndStatus(
            UUID projectId,
            SprintStatus status
    );

    Optional<Sprint> findByProjectIdAndStatus(
            UUID projectId,
            SprintStatus status
    );

    long countByProjectId(
            UUID projectId
    );

    long countByProjectIdAndStatus(
            UUID projectId,
            SprintStatus status
    );

    long countByStatus(
            SprintStatus status
    );
}
