package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.meeting.dto.CreateProjectMeetingRequest;
import com.project.taskmanagement.meeting.dto.ProjectMeetingResponse;
import com.project.taskmanagement.meeting.dto.UpdateGoogleMeetLinkRequest;
import com.project.taskmanagement.meeting.service.ProjectMeetingService;
import com.project.taskmanagement.service.context.CurrentUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiTags.PROJECTS)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/meetings")
public class ProjectMeetingController {
    private final ProjectMeetingService service;
    private final CurrentUserService users;

    @PostMapping
    @Operation(summary = "Tạo cuộc họp cho dự án")
    public ApiResponseSever<ProjectMeetingResponse> create(@PathVariable UUID projectId, @Valid @RequestBody CreateProjectMeetingRequest r) {
        return ApiResponseSever.ok(service.create(projectId, users.getActiveCurrentUser(), r));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách cuộc họp của dự án")
    public ApiResponseSever<List<ProjectMeetingResponse>> list(@PathVariable UUID projectId) {
        return ApiResponseSever.ok(service.list(projectId, users.getActiveCurrentUser()));
    }

    @GetMapping("/{meetingId}")
    @Operation(summary = "Lấy chi tiết cuộc họp")
    public ApiResponseSever<ProjectMeetingResponse> detail(@PathVariable UUID projectId, @PathVariable UUID meetingId) {
        return ApiResponseSever.ok(service.detail(projectId, meetingId, users.getActiveCurrentUser()));
    }

    @PatchMapping("/{meetingId}/google-meet-link")
    @Operation(summary = "Cập nhật link Google Meet")
    public ApiResponseSever<ProjectMeetingResponse> update(@PathVariable UUID projectId, @PathVariable UUID meetingId, @Valid @RequestBody UpdateGoogleMeetLinkRequest r) {
        return ApiResponseSever.ok(service.updateLink(projectId, meetingId, r, users.getActiveCurrentUser()));
    }
}
