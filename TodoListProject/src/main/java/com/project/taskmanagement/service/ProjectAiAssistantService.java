package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.ai.ProjectAiAskRequest;
import com.project.taskmanagement.dto.response.ai.ProjectAiAskResponse;

import java.util.UUID;

public interface ProjectAiAssistantService {
    ProjectAiAskResponse ask(UUID projectId, ProjectAiAskRequest request);
}
