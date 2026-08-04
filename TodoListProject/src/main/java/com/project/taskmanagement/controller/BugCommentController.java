package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.bug.CreateBugCommentRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugCommentRequest;
import com.project.taskmanagement.dto.response.bug.BugCommentResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BugCommentService;
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

@Tag(name = OpenApiTags.BUGS, description = "Bình luận và trao đổi trên Bug")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/bugs/{bugId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BugCommentController {

    BugCommentService bugCommentService;

    @Operation(summary = "Them binh luan Bug")
    @PostMapping
    public ApiResponseSever<BugCommentResponse> create(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @Valid @RequestBody CreateBugCommentRequest request
    ) {
        return ApiResponseSever.ok(bugCommentService.create(projectId, bugId, request));
    }

    @Operation(summary = "Danh sach binh luan Bug")
    @GetMapping
    public ApiResponseSever<List<BugCommentResponse>> getAll(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId
    ) {
        return ApiResponseSever.ok(bugCommentService.getAll(projectId, bugId));
    }

    @Operation(summary = "Cap nhat binh luan Bug")
    @PatchMapping("/{commentId}")
    public ApiResponseSever<BugCommentResponse> update(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateBugCommentRequest request
    ) {
        return ApiResponseSever.ok(bugCommentService.update(projectId, bugId, commentId, request));
    }

    @Operation(summary = "Xoa binh luan Bug")
    @DeleteMapping("/{commentId}")
    public ApiResponseSever<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @PathVariable UUID commentId
    ) {
        bugCommentService.delete(projectId, bugId, commentId);
        return ApiResponseSever.ok();
    }
}
