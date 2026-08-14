package com.project.taskmanagement.service;

import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesResponse;

import java.util.UUID;

public interface MeetingMinutesAiService {
    MeetingMinutesResponse create(UUID projectId, MeetingMinutesRequest request);
}
