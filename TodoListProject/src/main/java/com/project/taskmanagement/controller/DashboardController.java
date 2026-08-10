package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.dashboard.MyTaskSearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.dashboard.MyDashboardResponse;
import com.project.taskmanagement.dto.response.dashboard.MyTaskPageResponse;
import com.project.taskmanagement.dto.response.dashboard.MyTimeSummaryResponse;
import com.project.taskmanagement.service.MyDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = OpenApiTags.DASHBOARD, description = "Dashboard cá nhân, Task và Time Log của người dùng hiện tại")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class DashboardController {

    MyDashboardService myDashboardService;

    // ===================== MY DASHBOARD =====================

    @Operation(
            summary = "Lấy Dashboard cá nhân"
    )
    @GetMapping("/me")
    public ApiResponseSever<MyDashboardResponse>
    getMyDashboard() {

        return ApiResponseSever.ok(
                myDashboardService
                        .getMyDashboard()
        );
    }

    // ===================== MY TASKS =====================

    @Operation(
            summary = "Lấy danh sách Task của tôi"
    )
    @GetMapping("/me/tasks")
    public ApiResponseSever<MyTaskPageResponse>
    getMyTasks(
            @ParameterObject
            MyTaskSearchRequest request,

            @PageableDefault(
                    sort = "dueDate",
                    direction = Sort.Direction.ASC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                myDashboardService
                        .getMyTasks(
                                request,
                                pageable
                        )
        );
    }

    // ===================== MY TIME =====================

    @Operation(
            summary = "Lấy thống kê thời gian của tôi"
    )
    @GetMapping("/me/time-summary")
    public ApiResponseSever<MyTimeSummaryResponse>
    getMyTimeSummary() {

        return ApiResponseSever.ok(
                myDashboardService
                        .getMyTimeSummary()
        );
    }
}
