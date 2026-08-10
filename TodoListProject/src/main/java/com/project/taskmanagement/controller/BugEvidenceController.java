package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.bug.CreateBugEvidenceRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugEvidenceRequest;
import com.project.taskmanagement.dto.response.bug.BugEvidenceResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BugEvidenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiTags.BUGS, description = "Quản lý bằng chứng kiểm thử và tái hiện Bug")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/bugs/{bugId}/evidences")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugEvidenceController {

    BugEvidenceService bugEvidenceService;

    @Operation(summary = "Them bang chung Bug")
    @PostMapping
    public ApiResponseSever<BugEvidenceResponse> create(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @Valid @RequestBody CreateBugEvidenceRequest request
    ) {
        return ApiResponseSever.ok(bugEvidenceService.create(projectId, bugId, request));
    }

    @Operation(summary = "Danh sach bang chung Bug")
    @GetMapping
    public ApiResponseSever<List<BugEvidenceResponse>> getAll(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId
    ) {
        return ApiResponseSever.ok(bugEvidenceService.getAll(projectId, bugId));
    }

    @Operation(summary = "Cap nhat bang chung Bug")
    @PatchMapping("/{evidenceId}")
    public ApiResponseSever<BugEvidenceResponse> update(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID evidenceId,
            @Valid @RequestBody UpdateBugEvidenceRequest request
    ) {
        return ApiResponseSever.ok(bugEvidenceService.update(projectId, bugId, evidenceId, request));
    }

    @Operation(summary = "Xoa bang chung Bug")
    @DeleteMapping("/{evidenceId}")
    public ApiResponseSever<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID evidenceId
    ) {
        bugEvidenceService.delete(projectId, bugId, evidenceId);
        return ApiResponseSever.ok();
    }
}
