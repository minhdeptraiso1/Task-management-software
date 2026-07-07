package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.SprintReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SprintReviewRepository
        extends JpaRepository<SprintReview, UUID> {

    Optional<SprintReview> findByProjectIdAndSprintId(
            UUID projectId,
            UUID sprintId
    );
}
