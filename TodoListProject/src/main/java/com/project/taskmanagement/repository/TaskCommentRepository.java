package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskCommentRepository
        extends JpaRepository<TaskComment, UUID> {

    Optional<TaskComment> findByIdAndTaskId(
            UUID id,
            UUID taskId
    );

    Page<TaskComment>
    findAllByTaskIdAndParentCommentIdIsNullOrderByCreatedAtDesc(
            UUID taskId,
            Pageable pageable
    );

    List<TaskComment>
    findAllByParentCommentIdOrderByCreatedAtAsc(
            UUID parentCommentId
    );

    long countByParentCommentId(
            UUID parentCommentId
    );

    long countByTaskId(
            UUID taskId
    );
}