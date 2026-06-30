package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.enums.TaskImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskImportBatchRepository
        extends JpaRepository<TaskImportBatch, UUID> {

    Optional<TaskImportBatch> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    Page<TaskImportBatch>
    findAllByProjectIdOrderByCreatedAtDesc(
            UUID projectId,
            Pageable pageable
    );

    Page<TaskImportBatch>
    findAllByProjectIdAndSprintIdOrderByCreatedAtDesc(
            UUID projectId,
            UUID sprintId,
            Pageable pageable
    );

    long countByProjectIdAndStatus(
            UUID projectId,
            TaskImportStatus status
    );
}