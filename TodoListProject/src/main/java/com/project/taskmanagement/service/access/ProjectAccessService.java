package com.project.taskmanagement.service.access;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectAccessService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;

    public Project getProjectOrThrow(
            UUID projectId
    ) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.PROJECT_NOT_FOUND
                        )
                );
    }

    public Optional<ProjectMember> findMembership(
            UUID projectId,
            UUID userId
    ) {
        return projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                );
    }

    public ProjectMember getMembershipOrThrow(
            UUID projectId,
            UUID userId
    ) {
        return findMembership(projectId, userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.PROJECT_ACCESS_DENIED
                        )
                );
    }

    public void requireViewAccess(
            Project project,
            User user
    ) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        boolean isMember =
                projectMemberRepository
                        .existsByProjectIdAndUserId(
                                project.getId(),
                                user.getId()
                        );

        if (!isMember) {
            throw new BusinessException(
                    ErrorCode.PROJECT_ACCESS_DENIED
            );
        }
    }

    public ProjectMember requireMemberManagementAccess(
            UUID projectId,
            User currentUser
    ) {
        /*
         * Bảo đảm project tồn tại.
         */
        getProjectOrThrow(projectId);

        /*
         * ADMIN chỉ quản trị hệ thống,
         * không quản lý member trong flow project.
         */
        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_MANAGE_DENIED
            );
        }

        ProjectMember membership =
                getMembershipOrThrow(
                        projectId,
                        currentUser.getId()
                );

        boolean canManage =
                membership.getRole()
                        == ProjectMemberRole.OWNER
                        || membership.getRole()
                        == ProjectMemberRole.PROJECT_MANAGER;

        if (!canManage) {
            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_MANAGE_DENIED
            );
        }

        return membership;
    }

    public ProjectMember requireProjectUpdateAccess(
            UUID projectId,
            User currentUser
    ) {
        getProjectOrThrow(projectId);

        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.PROJECT_UPDATE_DENIED
            );
        }

        ProjectMember membership =
                getMembershipOrThrow(
                        projectId,
                        currentUser.getId()
                );

        boolean canUpdate =
                membership.getRole()
                        == ProjectMemberRole.OWNER
                        || membership.getRole()
                        == ProjectMemberRole.PROJECT_MANAGER;

        if (!canUpdate) {
            throw new BusinessException(
                    ErrorCode.PROJECT_UPDATE_DENIED
            );
        }

        return membership;
    }

    public ProjectMember requireProjectDeleteAccess(
            UUID projectId,
            User currentUser
    ) {
        getProjectOrThrow(projectId);

        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.PROJECT_DELETE_DENIED
            );
        }

        ProjectMember membership =
                getMembershipOrThrow(
                        projectId,
                        currentUser.getId()
                );

        if (membership.getRole()
                != ProjectMemberRole.OWNER) {

            throw new BusinessException(
                    ErrorCode.PROJECT_DELETE_DENIED
            );
        }

        return membership;
    }
}