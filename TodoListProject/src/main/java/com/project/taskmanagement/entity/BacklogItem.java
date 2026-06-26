package com.project.taskmanagement.entity;

import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "backlog_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at IS NULL")
public class BacklogItem extends BaseAuditEntity {

    @Column(
            name = "project_id",
            nullable = false
    )
    UUID projectId;

    /**
     * Null nghĩa là Backlog Item chưa được đưa vào Sprint.
     */
    @Column(name = "sprint_id")
    UUID sprintId;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    String title;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 30
    )
    BacklogItemType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    BacklogItemStatus status =
            BacklogItemStatus.DRAFT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            length = 30
    )
    BacklogPriority priority =
            BacklogPriority.MEDIUM;

    @Column(name = "story_points")
    Integer storyPoints;

    /**
     * Vị trí sắp xếp trong Product Backlog.
     * Số nhỏ hơn được ưu tiên hiển thị trước.
     */
    @Builder.Default
    @Column(
            name = "position",
            nullable = false
    )
    Long position = 0L;

    @Column(
            name = "created_by_user_id",
            nullable = false
    )
    UUID createdByUserId;
}