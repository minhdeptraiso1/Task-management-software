package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.ai.ProjectAiAskRequest;
import com.project.taskmanagement.dto.response.ai.ProjectAiAskResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.ProjectAiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = OpenApiTags.AI)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/ai")
public class ProjectAiAssistantController {
    private final ProjectAiAssistantService service;

    @Operation(summary = "Hỏi trợ lý AI theo phạm vi dự án")
    @PostMapping("/ask")
    public ApiResponseSever<ProjectAiAskResponse> ask(@PathVariable UUID projectId, @Valid @RequestBody ProjectAiAskRequest request) {
        return ApiResponseSever.ok(service.ask(projectId, request));
    }
}
