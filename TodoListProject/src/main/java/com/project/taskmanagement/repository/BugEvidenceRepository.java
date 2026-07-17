package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BugEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BugEvidenceRepository extends JpaRepository<BugEvidence, UUID> {

    Optional<BugEvidence> findByIdAndProjectIdAndBugId(UUID id, UUID projectId, UUID bugId);

    List<BugEvidence> findAllByProjectIdAndBugIdOrderByCreatedAtDesc(UUID projectId, UUID bugId);
}
