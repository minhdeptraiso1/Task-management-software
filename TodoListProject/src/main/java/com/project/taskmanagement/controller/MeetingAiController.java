package com.project.taskmanagement.controller;

import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingSuggestionResponse;
import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.MeetingAiSuggestionService;
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
public class MeetingAiController {
    private final MeetingAiSuggestionService service;

    @Operation(summary = "Gợi ý nội dung meeting bằng AI")
    @PostMapping("/meeting-suggestions")
    public ApiResponseSever<MeetingSuggestionResponse> suggest(@PathVariable UUID projectId, @Valid @RequestBody MeetingSuggestionRequest request) {
        return ApiResponseSever.ok(service.suggest(projectId, request));
    }
}
