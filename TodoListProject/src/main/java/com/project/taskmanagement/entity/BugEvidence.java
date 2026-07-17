package com.project.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "bug_evidences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class BugEvidence extends BaseAuditEntity {

    @Column(name = "project_id", nullable = false)
    UUID projectId;

    @Column(name = "bug_id", nullable = false)
    UUID bugId;

    @Column(name = "created_by_user_id", nullable = false)
    UUID createdByUserId;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "steps_to_reproduce", columnDefinition = "TEXT")
    String stepsToReproduce;

    @Column(name = "expected_result", columnDefinition = "TEXT")
    String expectedResult;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    String actualResult;

    @Column(name = "environment", columnDefinition = "TEXT")
    String environment;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;
}
