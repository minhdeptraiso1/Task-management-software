package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.ProjectStatus;
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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class Project extends BaseAuditEntity {

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 50
    )
    String code;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    String name;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    ProjectStatus status =
            ProjectStatus.PLANNING;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(
            name = "created_by_user_id",
            nullable = false
    )
    UUID createdByUserId;
}