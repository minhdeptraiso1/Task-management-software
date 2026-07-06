package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardActivityResponse;
import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardMemberWorkloadResponse;
import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardResponse;
import com.project.taskmanagement.service.ProjectDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/projects/{projectId}/dashboard"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectDashboardController {

    ProjectDashboardService
            projectDashboardService;

    // ===================== DASHBOARD =====================

    @Operation(
            summary = "Lấy Dashboard của Project"
    )
    @GetMapping
    public ApiResponseSever<ProjectDashboardResponse>
    getDashboard(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                projectDashboardService
                        .getDashboard(
                                projectId
                        )
        );
    }

    // ===================== WORKLOAD =====================

    @Operation(
            summary = "Lấy workload thành viên Project"
    )
    @GetMapping("/workload")
    public ApiResponseSever<
            List<ProjectDashboardMemberWorkloadResponse>
            > getWorkload(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                projectDashboardService
                        .getWorkload(
                                projectId
                        )
        );
    }

    // ===================== RECENT ACTIVITY =====================

    @Operation(
            summary = "Lấy hoạt động gần đây của Project"
    )
    @GetMapping("/recent-activities")
    public ApiResponseSever<
            List<ProjectDashboardActivityResponse>
            > getRecentActivities(
            @PathVariable
            UUID projectId,

            @RequestParam(
                    defaultValue = "10"
            )
            int limit
    ) {
        return ApiResponseSever.ok(
                projectDashboardService
                        .getRecentActivities(
                                projectId,
                                limit
                        )
        );
    }
}