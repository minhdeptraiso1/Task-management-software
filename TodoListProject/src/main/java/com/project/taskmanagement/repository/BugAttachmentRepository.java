package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BugAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BugAttachmentRepository extends JpaRepository<BugAttachment, UUID> {

    Optional<BugAttachment> findByIdAndProjectIdAndBugId(UUID id, UUID projectId, UUID bugId);

    List<BugAttachment> findAllByProjectIdAndBugIdOrderByCreatedAtDesc(UUID projectId, UUID bugId);
}
