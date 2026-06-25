package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.projectmember.AddProjectMemberRequest;
import com.project.taskmanagement.dto.request.projectmember.UpdateProjectMemberRoleRequest;
import com.project.taskmanagement.dto.response.projectmember.ProjectMemberResponse;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.ProjectMemberMapper;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.security.CurrentUser;
import com.project.taskmanagement.service.AuditLogService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.ProjectMemberService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.ProjectMemberValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectMemberServiceImpl
        implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;
    ProjectMemberMapper projectMemberMapper;

    ProjectAccessService projectAccessService;
    CurrentUserService currentUserService;
    AuditLogService auditLogService;

    ProjectActivityService projectActivityService;
    NotificationService notificationService;

    // ===================== ADD MEMBER =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.PROJECT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_SEARCH,
                    allEntries = true
            )
    })
    public ProjectMemberResponse addMember(
            UUID projectId,
            AddProjectMemberRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireMemberManagementAccess(
                        projectId,
                        currentUser
                );

        User targetUser = userRepository
                .findById(request.userId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        ProjectMemberValidator
                .validateRoleAssignment(
                        targetUser,
                        request.role()
                );

        boolean alreadyMember =
                projectMemberRepository
                        .existsByProjectIdAndUserId(
                                projectId,
                                targetUser.getId()
                        );

        if (alreadyMember) {
            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS
            );
        }

        ProjectMember member =
                ProjectMember.builder()
                        .projectId(projectId)
                        .userId(targetUser.getId())
                        .role(request.role())
                        .joinedAt(Instant.now())
                        .build();

        ProjectMember savedMember =
                projectMemberRepository.save(member);

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "userId",
                targetUser.getId()
        );

        newValue.put(
                "username",
                targetUser.getUsername()
        );

        newValue.put(
                "role",
                savedMember.getRole()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        savedMember.getId(),
                        ProjectActivityAction.MEMBER_ADDED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.PROJECT_MEMBER_ADDED,
                        "Bạn đã được thêm vào dự án",
                        "Bạn đã được thêm vào dự án với vai trò "
                                + savedMember.getRole(),
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        savedMember.getId(),
                        List.of(
                                targetUser.getId()
                        )
                )
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction.ADD_PROJECT_MEMBER.name()
        );

        return projectMemberMapper.toResponse(
                savedMember,
                targetUser
        );
    }

    // ===================== GET MEMBERS =====================

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMembers(
            UUID projectId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        /*
         * Chỉ cần có quyền xem project
         * là được xem danh sách thành viên.
         */
        var project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService.requireViewAccess(
                project,
                currentUser
        );

        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        );

        List<UUID> userIds = members
                .stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .toList();

        Map<UUID, User> usersById =
                userRepository
                        .findAllById(userIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        User::getId,
                                        Function.identity()
                                )
                        );

        return members
                .stream()
                .map(member -> {
                    User user =
                            usersById.get(
                                    member.getUserId()
                            );

                    if (user == null) {
                        throw new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        );
                    }

                    return projectMemberMapper
                            .toResponse(
                                    member,
                                    user
                            );
                })
                .toList();
    }

    // ===================== UPDATE ROLE =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.PROJECT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_SEARCH,
                    allEntries = true
            )
    })
    public ProjectMemberResponse updateMemberRole(
            UUID projectId,
            UUID memberId,
            UpdateProjectMemberRoleRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireMemberManagementAccess(
                        projectId,
                        currentUser
                );

        ProjectMember member =
                getMemberOrThrow(
                        projectId,
                        memberId
                );

        User targetUser = userRepository
                .findById(member.getUserId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        ProjectMemberValidator
                .validateRoleAssignment(
                        targetUser,
                        request.role()
                );

        /*
         * Nếu đang là OWNER cuối cùng thì không được
         * chuyển sang role khác.
         */
        if (member.getRole()
                == ProjectMemberRole.OWNER
                && request.role()
                != ProjectMemberRole.OWNER) {

            validateNotLastOwner(
                    projectId
            );
        }

        ProjectMemberRole oldRole =
                member.getRole();

        member.setRole(
                request.role()
        );


        ProjectMember savedMember =
                projectMemberRepository.save(member);


        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        savedMember.getId(),
                        ProjectActivityAction.MEMBER_ROLE_CHANGED,
                        currentUser.getId(),
                        Map.of(
                                "role",
                                oldRole
                        ),
                        Map.of(
                                "role",
                                savedMember.getRole()
                        )
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.PROJECT_MEMBER_ROLE_CHANGED,
                        "Vai trò dự án đã thay đổi",
                        "Vai trò của bạn trong dự án đã đổi từ "
                                + oldRole
                                + " thành "
                                + savedMember.getRole(),
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        savedMember.getId(),
                        List.of(
                                targetUser.getId()
                        )
                )
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction
                        .CHANGE_PROJECT_MEMBER_ROLE
                        .name()
        );

        return projectMemberMapper.toResponse(
                savedMember,
                targetUser
        );
    }

    // ===================== REMOVE MEMBER =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheNames.PROJECT_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_SEARCH,
                    allEntries = true
            )
    })
    public void removeMember(
            UUID projectId,
            UUID memberId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        projectAccessService
                .requireMemberManagementAccess(
                        projectId,
                        currentUser
                );

        ProjectMember member =
                getMemberOrThrow(
                        projectId,
                        memberId
                );

        /*
         * Tạm thời không cho người thực hiện
         * tự xóa chính mình.
         */
        if (member.getUserId()
                .equals(currentUser.getId())) {

            throw new BusinessException(
                    ErrorCode.PROJECT_MEMBER_CANNOT_REMOVE_SELF
            );
        }

        if (member.getRole()
                == ProjectMemberRole.OWNER) {

            validateNotLastOwner(
                    projectId
            );
        }

        User targetUser =
                userRepository
                        .findById(
                                member.getUserId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );
        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "userId",
                targetUser.getId()
        );

        oldValue.put(
                "username",
                targetUser.getUsername()
        );

        oldValue.put(
                "role",
                member.getRole()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        member.getId(),
                        ProjectActivityAction.MEMBER_REMOVED,
                        currentUser.getId(),
                        oldValue,
                        null
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.PROJECT_MEMBER_REMOVED,
                        "Bạn đã bị xóa khỏi dự án",
                        "Bạn đã bị xóa khỏi dự án",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.PROJECT_MEMBER,
                        member.getId(),
                        List.of(
                                targetUser.getId()
                        )
                )
        );
        member.markDeleted(
                CurrentUser.username()
        );

        projectMemberRepository.save(member);

        auditLogService.log(
                currentUser.getId(),
                AuditAction.REMOVE_PROJECT_MEMBER.name()
        );
    }

    // ===================== HELPER =====================

    private ProjectMember getMemberOrThrow(
            UUID projectId,
            UUID memberId
    ) {
        return projectMemberRepository
                .findByIdAndProjectId(
                        memberId,
                        projectId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.PROJECT_MEMBER_NOT_FOUND
                        )
                );
    }

    private void validateNotLastOwner(
            UUID projectId
    ) {
        long ownerCount =
                projectMemberRepository
                        .countByProjectIdAndRole(
                                projectId,
                                ProjectMemberRole.OWNER
                        );

        if (ownerCount <= 1) {
            throw new BusinessException(
                    ErrorCode
                            .PROJECT_LAST_OWNER_CANNOT_BE_REMOVED
            );
        }
    }
}