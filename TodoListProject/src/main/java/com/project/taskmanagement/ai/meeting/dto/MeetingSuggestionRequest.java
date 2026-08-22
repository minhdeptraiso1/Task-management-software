package com.project.taskmanagement.ai.meeting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MeetingSuggestionRequest {
    private MeetingType meetingType;
    private String additionalNote;

    @NotNull(message = "Loại meeting không được để trống")
    public MeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(MeetingType v) {
        meetingType = v;
    }

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    public String getAdditionalNote() {
        return additionalNote;
    }

    public void setAdditionalNote(String v) {
        additionalNote = v;
    }
}
 