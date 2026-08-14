package com.project.taskmanagement.controller;

import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesRequest;
import com.project.taskmanagement.ai.meeting.dto.MeetingMinutesResponse;
import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.MeetingMinutesAiService;
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
public class MeetingMinutesAiController {
    private final MeetingMinutesAiService service;

    @Operation(summary = "Tạo biên bản họp từ ghi chú bằng AI")
    @PostMapping("/meeting-minutes")
    public ApiResponseSever<MeetingMinutesResponse> create(@PathVariable UUID projectId, @Valid @RequestBody MeetingMinutesRequest request) {
        return ApiResponseSever.ok(service.create(projectId, request));
    }
}
