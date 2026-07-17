package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BugComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BugCommentRepository extends JpaRepository<BugComment, UUID> {

    Optional<BugComment> findByIdAndProjectIdAndBugId(UUID id, UUID projectId, UUID bugId);

    List<BugComment> findAllByProjectIdAndBugIdOrderByCreatedAtAsc(UUID projectId, UUID bugId);
}
