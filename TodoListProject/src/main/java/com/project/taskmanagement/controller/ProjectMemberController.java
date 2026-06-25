package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.projectmember.AddProjectMemberRequest;
import com.project.taskmanagement.dto.request.projectmember.UpdateProjectMemberRoleRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.projectmember.ProjectMemberResponse;
import com.project.taskmanagement.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectMemberController {

    ProjectMemberService projectMemberService;

    @Operation(
            summary = "Thêm thành viên vào dự án"
    )
    @PostMapping
    public ResponseEntity<
            ApiResponseSever<ProjectMemberResponse>
            > addMember(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            AddProjectMemberRequest request
    ) {
        ProjectMemberResponse response =
                projectMemberService.addMember(
                        projectId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseSever.ok(response)
                );
    }

    @Operation(
            summary = "Lấy danh sách thành viên dự án"
    )
    @GetMapping
    public ApiResponseSever<
            List<ProjectMemberResponse>
            > getMembers(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                projectMemberService.getMembers(
                        projectId
                )
        );
    }

    @Operation(
            summary = "Thay đổi vai trò thành viên"
    )
    @PatchMapping("/{memberId}/role")
    public ApiResponseSever<ProjectMemberResponse>
    updateMemberRole(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID memberId,

            @Valid
            @RequestBody
            UpdateProjectMemberRoleRequest request
    ) {
        return ApiResponseSever.ok(
                projectMemberService.updateMemberRole(
                        projectId,
                        memberId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa thành viên khỏi dự án"
    )
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID memberId
    ) {
        projectMemberService.removeMember(
                projectId,
                memberId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}