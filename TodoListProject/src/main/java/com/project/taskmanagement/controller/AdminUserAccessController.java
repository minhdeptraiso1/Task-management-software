package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.admin.UpdateUserRoleRequest;
import com.project.taskmanagement.dto.response.admin.AdminUserResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.service.AdminUserAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.ADMIN, description = "Quản trị tài khoản, trạng thái và quyền truy cập người dùng")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminUserAccessController {

    AdminUserAccessService adminUserAccessService;

    @Operation(summary = "Bat tai khoan user")
    @PatchMapping("/{userId}/enable")
    public ApiResponseSever<AdminUserResponse> enableUser(
            @PathVariable
            UUID userId
    ) {
        return ApiResponseSever.ok(
                adminUserAccessService.enableUser(userId)
        );
    }

    @Operation(summary = "Disable tai khoan user")
    @PatchMapping("/{userId}/disable")
    public ApiResponseSever<AdminUserResponse> disableUser(
            @PathVariable
            UUID userId
    ) {
        return ApiResponseSever.ok(
                adminUserAccessService.disableUser(userId)
        );
    }

    @Operation(summary = "Doi system role cua user")
    @PatchMapping("/{userId}/role")
    public ApiResponseSever<AdminUserResponse> updateRole(
            @PathVariable
            UUID userId,

            @Valid
            @RequestBody
            UpdateUserRoleRequest request
    ) {
        return ApiResponseSever.ok(
                adminUserAccessService.updateRole(
                        userId,
                        request
                )
        );
    }
}
