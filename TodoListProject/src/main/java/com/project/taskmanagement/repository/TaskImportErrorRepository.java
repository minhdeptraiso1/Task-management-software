package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskImportError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskImportErrorRepository
        extends JpaRepository<TaskImportError, UUID> {

    Page<TaskImportError>
    findAllByImportBatchIdOrderByRowNumberAscCreatedAtAsc(
            UUID importBatchId,
            Pageable pageable
    );

    List<TaskImportError>
    findTop10ByImportBatchIdOrderByRowNumberAscCreatedAtAsc(
            UUID importBatchId
    );

}
