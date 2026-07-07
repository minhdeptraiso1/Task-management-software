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
@Table(name = "sprint_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class SprintReview extends BaseAuditEntity {

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

    @Column(name = "goal_achieved")
    Boolean goalAchieved;

    @Column(
            name = "demo_summary",
            columnDefinition = "TEXT"
    )
    String demoSummary;

    @Column(
            name = "stakeholder_feedback",
            columnDefinition = "TEXT"
    )
    String stakeholderFeedback;

    @Column(
            name = "accepted_item_summary",
            columnDefinition = "TEXT"
    )
    String acceptedItemSummary;

    @Column(
            name = "rejected_item_summary",
            columnDefinition = "TEXT"
    )
    String rejectedItemSummary;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    String note;
}
