package com.project.taskmanagement.service;

import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionResponse;

import java.util.UUID;

public interface ActionItemAiService {
    ActionItemSuggestionResponse suggest(UUID projectId, ActionItemSuggestionRequest request);
}
