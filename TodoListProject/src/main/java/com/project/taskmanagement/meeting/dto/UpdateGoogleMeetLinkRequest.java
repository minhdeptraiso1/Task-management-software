package com.project.taskmanagement.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGoogleMeetLinkRequest(
        @NotBlank(message = "Link Google Meet không được để trống") @Size(max = 500) String googleMeetLink) {
}
