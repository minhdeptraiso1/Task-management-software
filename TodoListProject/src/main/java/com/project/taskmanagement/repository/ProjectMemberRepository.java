package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.enums.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, UUID> {

    boolean existsByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    Optional<ProjectMember> findByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    Optional<ProjectMember> findByIdAndProjectId(
            UUID id,
            UUID projectId
    );

    List<ProjectMember> findAllByProjectIdOrderByJoinedAtAsc(
            UUID projectId
    );

    List<ProjectMember> findAllByProjectIdAndRole(
            UUID projectId,
            ProjectMemberRole role
    );

    long countByProjectIdAndRole(
            UUID projectId,
            ProjectMemberRole role
    );

    @Query("""
            SELECT pm.projectId
            FROM ProjectMember pm
            WHERE pm.userId = :userId
            """)
    List<UUID> findProjectIdsByUserId(
            @Param("userId")
            UUID userId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE ProjectMember pm
            SET pm.deletedAt = :deletedAt,
                pm.deletedBy = :deletedBy
            WHERE pm.projectId = :projectId
              AND pm.deletedAt IS NULL
            """)
    int softDeleteAllByProjectId(
            @Param("projectId")
            UUID projectId,

            @Param("deletedAt")
            Instant deletedAt,

            @Param("deletedBy")
            String deletedBy
    );

    long countByUserId(
            UUID userId
    );

    long countByProjectId(
            UUID projectId
    );

    boolean existsByUserIdAndRoleIn(
            UUID userId,
            Collection<ProjectMemberRole> roles
    );
}
