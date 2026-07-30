package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
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

    List<ProjectActivityLog>
    findAllByProjectIdAndEntityTypeAndEntityIdInAndActionInAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            UUID projectId,
            ActivityEntityType entityType,
            Collection<UUID> entityIds,
            Collection<ProjectActivityAction> actions,
            Instant createdAt
    );
}
