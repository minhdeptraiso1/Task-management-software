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
@Table(name = "bug_comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class BugComment extends BaseAuditEntity {

    @Column(name = "project_id", nullable = false)
    UUID projectId;

    @Column(name = "bug_id", nullable = false)
    UUID bugId;

    @Column(name = "parent_id")
    UUID parentId;

    @Column(name = "author_user_id", nullable = false)
    UUID authorUserId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    String content;
}
