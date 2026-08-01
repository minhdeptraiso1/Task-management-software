package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.admin.UpdateUserRoleRequest;
import com.project.taskmanagement.dto.response.admin.AdminUserActivityPageResponse;
import com.project.taskmanagement.dto.response.admin.AdminUserActivityResponse;
import com.project.taskmanagement.dto.response.admin.AdminUserResponse;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ProjectMemberRole;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.TokenSessionRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.AdminUserAccessService;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AdminUserAccessServiceImpl
        implements AdminUserAccessService {

    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectActivityLogRepository projectActivityLogRepository;
    TokenSessionRepository tokenSessionRepository;
    CurrentUserService currentUserService;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    static final List<ProjectMemberRole> PROJECT_MANAGER_ROLES =
            List.of(
                    ProjectMemberRole.OWNER,
                    ProjectMemberRole.PROJECT_MANAGER
            );

    @Override
    @Transactional
    public AdminUserResponse enableUser(UUID userId) {
        User currentAdmin =
                requireAdmin();

        User targetUser =
                getUserOrThrow(userId);

        Map<String, Object> oldValue =
                snapshot(targetUser);

        targetUser.setEnabled(true);

        User saved =
                userRepository.save(targetUser);

        logUserAudit(
                currentAdmin,
                SystemAuditAction.USER_ENABLED,
                saved,
                oldValue,
                snapshot(saved)
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse disableUser(UUID userId) {
        User currentAdmin =
                requireAdmin();

        User targetUser =
                getUserOrThrow(userId);

        validateNotSelf(currentAdmin, targetUser);
        validateNotLastAdminWhenDisable(targetUser);

        Map<String, Object> oldValue =
                snapshot(targetUser);

        targetUser.setEnabled(false);
        targetUser.setLogoutAllAt(Instant.now());

        User saved =
                userRepository.save(targetUser);

        tokenSessionRepository.revokeAllByUserId(saved.getId());

        logUserAudit(
                currentAdmin,
                SystemAuditAction.USER_DISABLED,
                saved,
                oldValue,
                snapshot(saved)
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse updateRole(
            UUID userId,
            UpdateUserRoleRequest request
    ) {
        User currentAdmin =
                requireAdmin();

        User targetUser =
                getUserOrThrow(userId);

        UserRole oldRole =
                targetUser.getRole();

        UserRole newRole =
                request.role();

        if (oldRole == newRole) {
            return toResponse(targetUser);
        }

        Map<String, Object> oldValue =
                snapshot(targetUser);

        validateNotLastAdminWhenDemote(
                targetUser,
                newRole
        );
        validateProjectRoleCompatibility(
                targetUser,
                newRole
        );

        targetUser.setRole(newRole);

        User saved =
                userRepository.save(targetUser);

        logUserAudit(
                currentAdmin,
                SystemAuditAction.USER_ROLE_CHANGED,
                saved,
                oldValue,
                snapshot(saved)
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserActivityPageResponse getUserActivities(
            UUID userId,
            Pageable pageable
    ) {
        User currentAdmin =
                requireAdmin();
        getUserOrThrow(userId);

        Page<AdminUserActivityResponse> page =
                projectActivityLogRepository
                        .findAllByPerformedByUserIdOrderByCreatedAtDesc(
                                userId,
                                pageable
                        )
                        .map(this::toActivityResponse);

        AdminUserActivityPageResponse response =
                AdminUserActivityPageResponse.from(page);

        systemAuditService.log(
                new SystemAuditCommand(
                        currentAdmin.getId(),
                        SystemAuditAction.ADMIN_VIEWED_USER_ACTIVITY,
                        SystemAuditResourceType.USER,
                        userId,
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "targetUserId",
                                userId,
                                "page",
                                pageable.getPageNumber(),
                                "size",
                                pageable.getPageSize()
                        ),
                        true,
                        null
                )
        );

        return response;
    }

    private User requireAdmin() {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.ADMIN_ONLY
            );
        }

        return currentUser;
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private void validateNotSelf(
            User currentAdmin,
            User targetUser
    ) {
        if (currentAdmin.getId().equals(targetUser.getId())) {
            throw new BusinessException(
                    ErrorCode.USER_CANNOT_DISABLE_SELF
            );
        }
    }

    private void validateNotLastAdminWhenDisable(User targetUser) {
        if (targetUser.getRole() != UserRole.ADMIN) {
            return;
        }

        long activeAdminCount =
                userRepository.countByRoleAndEnabledTrue(
                        UserRole.ADMIN
                );

        if (activeAdminCount <= 1) {
            throw new BusinessException(
                    ErrorCode.USER_CANNOT_DISABLE_LAST_ADMIN
            );
        }
    }

    private void validateNotLastAdminWhenDemote(
            User targetUser,
            UserRole newRole
    ) {
        if (targetUser.getRole() != UserRole.ADMIN
                || newRole == UserRole.ADMIN) {
            return;
        }

        long activeAdminCount =
                userRepository.countByRoleAndEnabledTrue(
                        UserRole.ADMIN
                );

        if (activeAdminCount <= 1) {
            throw new BusinessException(
                    ErrorCode.USER_CANNOT_DEMOTE_LAST_ADMIN
            );
        }
    }

    private void validateProjectRoleCompatibility(
            User targetUser,
            UserRole newRole
    ) {
        if (newRole == UserRole.ADMIN
                || newRole == UserRole.MANAGER) {
            return;
        }

        boolean hasManagerProjectRole =
                projectMemberRepository.existsByUserIdAndRoleIn(
                        targetUser.getId(),
                        PROJECT_MANAGER_ROLES
                );

        if (hasManagerProjectRole) {
            throw new BusinessException(
                    ErrorCode.USER_ROLE_CONFLICT_WITH_PROJECT_ROLE
            );
        }
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void logUserAudit(
            User currentAdmin,
            SystemAuditAction action,
            User targetUser,
            Map<String, Object> oldValue,
            Map<String, Object> newValue
    ) {
        systemAuditService.log(
                new SystemAuditCommand(
                        currentAdmin.getId(),
                        action,
                        SystemAuditResourceType.USER,
                        targetUser.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        oldValue,
                        newValue,
                        true,
                        null
                )
        );
    }

    private Map<String, Object> snapshot(User user) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put("id", user.getId());
        value.put("username", user.getUsername());
        value.put("email", user.getEmail());
        value.put("role", user.getRole());
        value.put("enabled", user.isEnabled());
        value.put("logoutAllAt", user.getLogoutAllAt());
        value.put("createdAt", user.getCreatedAt());
        value.put("updatedAt", user.getUpdatedAt());

        return value;
    }

    private AdminUserActivityResponse toActivityResponse(
            ProjectActivityLog log
    ) {
        return new AdminUserActivityResponse(
                log.getId(),
                log.getProjectId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getPerformedByUserId(),
                log.getOldValueJson(),
                log.getNewValueJson(),
                log.getCreatedAt()
        );
    }
}
