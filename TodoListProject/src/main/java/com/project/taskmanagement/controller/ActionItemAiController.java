package com.project.taskmanagement.controller;

import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.ActionItemSuggestionResponse;
import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.ActionItemAiService;
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
public class ActionItemAiController {
    private final ActionItemAiService service;

    @Operation(summary = "Đề xuất action item sau meeting bằng AI")
    @PostMapping("/action-items")
    public ApiResponseSever<ActionItemSuggestionResponse> suggest(@PathVariable UUID projectId, @Valid @RequestBody ActionItemSuggestionRequest request) {
        return ApiResponseSever.ok(service.suggest(projectId, request));
    }
}
