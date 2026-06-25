package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.ProjectMemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class ProjectMember extends BaseAuditEntity {

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    @Column(
            name = "user_id",
            nullable = false
    )
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 30
    )
    ProjectMemberRole role;

    @Builder.Default
    @Column(
            name = "joined_at",
            nullable = false
    )
    Instant joinedAt = Instant.now();
}