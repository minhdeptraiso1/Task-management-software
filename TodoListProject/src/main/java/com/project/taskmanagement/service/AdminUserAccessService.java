package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.admin.UpdateUserRoleRequest;
import com.project.taskmanagement.dto.response.admin.AdminUserActivityPageResponse;
import com.project.taskmanagement.dto.response.admin.AdminUserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserAccessService {

    AdminUserResponse enableUser(
            UUID userId
    );

    AdminUserResponse disableUser(
            UUID userId
    );

    AdminUserResponse updateRole(
            UUID userId,
            UpdateUserRoleRequest request
    );

    AdminUserActivityPageResponse getUserActivities(
            UUID userId,
            Pageable pageable
    );
}
