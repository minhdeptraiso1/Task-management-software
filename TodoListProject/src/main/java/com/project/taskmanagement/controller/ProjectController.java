package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.project.CreateProjectRequest;
import com.project.taskmanagement.dto.request.project.ProjectSearchRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectStatusRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectResponse;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectController {

    ProjectService projectService;
    ProjectActivityService projectActivityService;

    @Operation(
            summary = "Tạo dự án",
            description = """
                    Chỉ tài khoản có role hệ thống MANAGER
                    được phép tạo dự án.
                    
                    Người tạo dự án tự động trở thành OWNER
                    trong project_members.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tạo dự án thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu dự án không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền tạo dự án"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Mã dự án đã tồn tại"
            )
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<
            ApiResponseSever<ProjectResponse>
            > createProject(
            @Valid
            @RequestBody
            CreateProjectRequest request
    ) {
        ProjectResponse response =
                projectService.createProject(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseSever.ok(response)
                );
    }

    @Operation(
            summary = "Lấy danh sách dự án được phép xem",
            description = """
                    ADMIN xem được toàn bộ dự án.
                    
                    MANAGER và EMPLOYEE chỉ xem được
                    những dự án mà mình đang là thành viên.
                    """
    )
    @GetMapping
    public ApiResponseSever<ProjectPageResponse> searchProjects(
            @ParameterObject
            ProjectSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                projectService.searchProjects(
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Xem chi tiết dự án",
            description = """
                    ADMIN được xem mọi dự án.
                    
                    MANAGER và EMPLOYEE phải là thành viên
                    của dự án mới được xem.
                    """
    )
    @GetMapping("/{projectId}")
    public ApiResponseSever<ProjectResponse> getProjectById(
            @Parameter(
                    description = "ID dự án"
            )
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                projectService.getProjectById(
                        projectId
                )
        );
    }

    @Operation(
            summary = "Cập nhật thông tin dự án",
            description = """
                    Chỉ OWNER hoặc PROJECT_MANAGER
                    được phép cập nhật dự án.
                    """
    )
    @PutMapping("/{projectId}")
    public ApiResponseSever<ProjectResponse> updateProject(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            UpdateProjectRequest request
    ) {
        return ApiResponseSever.ok(
                projectService.updateProject(
                        projectId,
                        request
                )
        );
    }

    @Operation(
            summary = "Thay đổi trạng thái dự án"
    )
    @PatchMapping("/{projectId}/status")
    public ApiResponseSever<ProjectResponse>
    updateProjectStatus(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            UpdateProjectStatusRequest request
    ) {
        return ApiResponseSever.ok(
                projectService.updateProjectStatus(
                        projectId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa mềm dự án",
            description = """
                    Chỉ OWNER được phép xóa dự án.
                    Dự án và thành viên dự án sẽ bị soft delete.
                    """
    )
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable
            UUID projectId
    ) {
        projectService.deleteProject(
                projectId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Xem lịch sử hoạt động của dự án",
            description = """
                    ADMIN được xem toàn bộ lịch sử dự án.
                    
                    MANAGER và EMPLOYEE phải là thành viên
                    của dự án mới được xem.
                    """
    )
    @GetMapping("/{projectId}/activities")
    public ApiResponseSever<ProjectActivityPageResponse>
    getProjectActivities(
            @PathVariable
            UUID projectId,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                projectActivityService
                        .getActivities(
                                projectId,
                                pageable
                        )
        );
    }

}