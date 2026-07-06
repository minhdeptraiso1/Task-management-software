package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.ProjectActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProjectActivityLogRepository
        extends JpaRepository<ProjectActivityLog, UUID>,
        JpaSpecificationExecutor<ProjectActivityLog> {

    Page<ProjectActivityLog>
    findAllByProjectIdOrderByCreatedAtDesc(
            UUID projectId,
            Pageable pageable
    );

    Optional<ProjectActivityLog>
    findByIdAndProjectId(
            UUID id,
            UUID projectId
    );
}