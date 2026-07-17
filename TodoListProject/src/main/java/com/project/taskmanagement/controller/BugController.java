package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.bug.AssignBugRequest;
import com.project.taskmanagement.dto.request.bug.BugSearchRequest;
import com.project.taskmanagement.dto.request.bug.CreateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugSeverityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugPriorityRequest;
import com.project.taskmanagement.dto.request.bug.UpdateBugStatusRequest;
import com.project.taskmanagement.dto.response.bug.BugPageResponse;
import com.project.taskmanagement.dto.response.bug.BugResponse;
import com.project.taskmanagement.dto.response.bug.BugSummaryResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.BugService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/bugs")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class BugController {

    BugService bugService;

    @Operation(summary = "Tạo Bug")
    @PostMapping
    public ApiResponseSever<BugResponse> create(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            CreateBugRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.create(projectId, request)
        );
    }

    @Operation(summary = "Danh sách Bug")
    @GetMapping
    public ApiResponseSever<BugPageResponse> search(
            @PathVariable
            UUID projectId,

            @ParameterObject
            BugSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                bugService.search(projectId, request, pageable)
        );
    }

    @Operation(summary = "Chi tiết Bug")
    @GetMapping("/{bugId}")
    public ApiResponseSever<BugResponse> getById(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID bugId
    ) {
        return ApiResponseSever.ok(
                bugService.getById(projectId, bugId)
        );
    }

    @Operation(summary = "Cập nhật Bug")
    @PatchMapping("/{bugId}")
    public ApiResponseSever<BugResponse> update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID bugId,

            @Valid
            @RequestBody
            UpdateBugRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.update(projectId, bugId, request)
        );
    }

    @Operation(summary = "Gán người xử lý Bug")
    @PatchMapping("/{bugId}/assign")
    public ApiResponseSever<BugResponse> assign(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID bugId,

            @Valid
            @RequestBody
            AssignBugRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.assign(projectId, bugId, request)
        );
    }

    @Operation(summary = "Cập nhật trạng thái Bug")
    @PatchMapping("/{bugId}/status")
    public ApiResponseSever<BugResponse> updateStatus(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID bugId,

            @Valid
            @RequestBody
            UpdateBugStatusRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.updateStatus(projectId, bugId, request)
        );
    }

    @Operation(summary = "Gỡ gán người xử lý Bug")
    @PatchMapping("/{bugId}/unassign")
    public ApiResponseSever<BugResponse> unassign(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId
    ) {
        return ApiResponseSever.ok(
                bugService.unassign(projectId, bugId)
        );
    }

    @Operation(summary = "Cập nhật mức độ nghiêm trọng Bug")
    @PatchMapping("/{bugId}/severity")
    public ApiResponseSever<BugResponse> updateSeverity(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @Valid @RequestBody UpdateBugSeverityRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.updateSeverity(projectId, bugId, request)
        );
    }

    @Operation(summary = "Cập nhật mức độ ưu tiên Bug")
    @PatchMapping("/{bugId}/priority")
    public ApiResponseSever<BugResponse> updatePriority(
            @PathVariable UUID projectId,
            @PathVariable UUID bugId,
            @Valid @RequestBody UpdateBugPriorityRequest request
    ) {
        return ApiResponseSever.ok(
                bugService.updatePriority(projectId, bugId, request)
        );
    }

    @Operation(summary = "Xóa Bug")
    @DeleteMapping("/{bugId}")
    public ApiResponseSever<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID bugId
    ) {
        bugService.delete(projectId, bugId);

        return ApiResponseSever.ok(null);
    }

    @Operation(summary = "Tổng hợp Bug của Project")
    @GetMapping("/summary")
    public ApiResponseSever<BugSummaryResponse> getSummary(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                bugService.getSummary(projectId)
        );
    }
}
