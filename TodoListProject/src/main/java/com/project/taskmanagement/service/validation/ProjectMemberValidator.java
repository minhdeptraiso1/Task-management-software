package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;

public final class ProjectMemberValidator {

    private ProjectMemberValidator() {
    }

    public static void validateRoleAssignment(
            User targetUser,
            ProjectMemberRole projectRole
    ) {
        if (targetUser == null) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        if (!targetUser.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        /*
         * ADMIN không tham gia project
         * trong flow nghiệp vụ thông thường.
         */
        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_ROLE_NOT_ALLOWED
            );
        }

        boolean managementRole =
                projectRole == ProjectMemberRole.OWNER
                        || projectRole
                        == ProjectMemberRole.PROJECT_MANAGER;

        /*
         * OWNER và PROJECT_MANAGER chỉ được
         * gán cho tài khoản hệ thống MANAGER.
         */
        if (managementRole
                && targetUser.getRole()
                != UserRole.MANAGER) {

            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_ROLE_NOT_ALLOWED
            );
        }
    }
}