package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
