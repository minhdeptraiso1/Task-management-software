package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskTimeLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DatabaseLockRepository
        extends Repository<TaskTimeLog, UUID> {

    @Query(
            value = "SELECT pg_advisory_xact_lock(:lockKey)",
            nativeQuery = true
    )
    void acquireTransactionLock(
            @Param("lockKey") Long lockKey
    );
}
