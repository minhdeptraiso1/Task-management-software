package com.project.taskmanagement.service;

import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionResponse;

import java.util.UUID;

public interface MeetingAiSuggestionService {
    MeetingSuggestionResponse suggest(UUID projectId, MeetingSuggestionRequest request);
}
