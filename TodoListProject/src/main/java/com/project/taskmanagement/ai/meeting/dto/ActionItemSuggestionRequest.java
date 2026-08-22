package com.project.taskmanagement.ai.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ActionItemSuggestionRequest {
    private MeetingType meetingType;
    private String meetingTitle, meetingContent, additionalNote;

    @NotNull(message = "Loại meeting không được để trống")
    public MeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(MeetingType v) {
        meetingType = v;
    }

    @Size(max = 255)
    public String getMeetingTitle() {
        return meetingTitle;
    }

    public void setMeetingTitle(String v) {
        meetingTitle = v;
    }

    @NotBlank(message = "Nội dung cuộc họp không được để trống")
    @Size(min = 20, max = 12000, message = "Nội dung cuộc họp phải từ 20 đến 12000 ký tự")
    public String getMeetingContent() {
        return meetingContent;
    }

    public void setMeetingContent(String v) {
        meetingContent = v;
    }

    @Size(max = 1000)
    public String getAdditionalNote() {
        return additionalNote;
    }

    public void setAdditionalNote(String v) {
        additionalNote = v;
    }
}
 