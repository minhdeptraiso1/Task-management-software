package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.admin.AdminDashboardResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = OpenApiTags.ADMIN, description = "Dashboard quản trị hệ thống")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminDashboardController {

    AdminDashboardService adminDashboardService;

    @Operation(summary = "Lấy Admin Dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponseSever<AdminDashboardResponse> getDashboard() {
        return ApiResponseSever.ok(
                adminDashboardService.getDashboard()
        );
    }
}
