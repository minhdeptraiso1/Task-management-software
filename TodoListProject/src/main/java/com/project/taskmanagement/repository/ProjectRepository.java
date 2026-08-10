package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository
        extends JpaRepository<Project, UUID>,
        JpaSpecificationExecutor<Project> {

    Optional<Project> findByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCase(
            String code
    );

    long countByStatus(
            ProjectStatus status
    );
}
