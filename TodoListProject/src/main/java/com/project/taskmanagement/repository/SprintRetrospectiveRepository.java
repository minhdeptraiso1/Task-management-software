package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.SprintRetrospective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SprintRetrospectiveRepository
        extends JpaRepository<SprintRetrospective, UUID> {

    Optional<SprintRetrospective> findByProjectIdAndSprintId(
            UUID projectId,
            UUID sprintId
    );
}
