package com.project.taskmanagement.meeting.entity;

import com.project.taskmanagement.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_meetings")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMeeting extends BaseAuditEntity {
    @Column(name = "project_id", nullable = false)
    UUID projectId;
    @Column(nullable = false, length = 255)
    String title;
    @Column(length = 2000)
    String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 50)
    ProjectMeetingType meetingType;
    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    LocalDateTime endTime;
    @Column(name = "google_meet_link", length = 500)
    String googleMeetLink;
    @Column(name = "created_by_user_id", nullable = false)
    UUID createdByUserId;
}
