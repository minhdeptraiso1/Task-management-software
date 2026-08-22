package com.project.taskmanagement.meeting.dto;

import com.project.taskmanagement.meeting.entity.ProjectMeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateProjectMeetingRequest {
    private String title, description, googleMeetLink;
    private ProjectMeetingType meetingType;
    private LocalDateTime startTime, endTime;

    @NotBlank(message = "Tiêu đề meeting không được để trống")
    @Size(max = 255)
    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    @Size(max = 2000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    @NotNull(message = "Loại meeting không được để trống")
    public ProjectMeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(ProjectMeetingType v) {
        meetingType = v;
    }

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime v) {
        startTime = v;
    }

    @NotNull(message = "Thời gian kết thúc không được để trống")
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime v) {
        endTime = v;
    }

    @Size(max = 500)
    public String getGoogleMeetLink() {
        return googleMeetLink;
    }

    public void setGoogleMeetLink(String v) {
        googleMeetLink = v;
    }
}
