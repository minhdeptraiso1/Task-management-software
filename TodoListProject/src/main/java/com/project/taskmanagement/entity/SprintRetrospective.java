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
@Table(name = "sprint_retrospectives")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class SprintRetrospective extends BaseAuditEntity {

    @Column(
            name = "sprint_id",
            nullable = false
    )
    UUID sprintId;

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    @Column(
            name = "went_well",
            columnDefinition = "TEXT"
    )
    String wentWell;

    @Column(
            name = "went_wrong",
            columnDefinition = "TEXT"
    )
    String wentWrong;

    @Column(
            name = "improvement",
            columnDefinition = "TEXT"
    )
    String improvement;

    @Column(
            name = "action_items_json",
            columnDefinition = "TEXT"
    )
    String actionItemsJson;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    String note;
}
