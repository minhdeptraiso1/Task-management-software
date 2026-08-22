package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.AiRequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, UUID> {
    Page<AiRequestLog> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
