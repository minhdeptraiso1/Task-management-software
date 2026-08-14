package com.project.taskmanagement.meeting.dto;

import com.project.taskmanagement.meeting.entity.ProjectMeetingType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectMeetingResponse(UUID id, UUID projectId, String title, String description,
                                     ProjectMeetingType meetingType, LocalDateTime startTime, LocalDateTime endTime,
                                     String googleMeetLink, boolean hasGoogleMeetLink, UUID createdByUserId,
                                     Instant createdAt) {
}
