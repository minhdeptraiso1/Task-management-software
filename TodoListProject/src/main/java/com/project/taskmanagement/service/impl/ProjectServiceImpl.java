package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.project.CreateProjectRequest;
import com.project.taskmanagement.dto.request.project.ProjectSearchRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectStatusRequest;
import com.project.taskmanagement.dto.response.project.ProjectPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.*;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.ProjectMapper;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.ProjectRepository;
import com.project.taskmanagement.repository.spec.ProjectSpecification;
import com.project.taskmanagement.service.AuditLogService;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.ProjectService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.NotificationCommand;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import com.project.taskmanagement.service.validation.DateRangeValidator;
import com.project.taskmanagement.service.validation.PageableValidator;
import com.project.taskmanagement.service.validation.ProjectValidator;
import com.project.taskmanagement.util.TextNormalizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectServiceImpl
        implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectMapper projectMapper;
    AuditLogService auditLogService;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityService projectActivityService;
    NotificationService notificationService;

    // ===================== CREATE PROJECT =====================

    @Override
    @Transactional
    @CacheEvict(
            value = CacheNames.PROJECT_SEARCH,
            allEntries = true
    )
    public ProjectResponse createProject(
            CreateProjectRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        ProjectValidator.validateCreator(
                currentUser
        );

        ProjectValidator.validateDates(
                request.startDate(),
                request.endDate()
        );

        String normalizedCode =
                TextNormalizer.uppercase(
                        request.code()
                );

        String normalizedName =
                TextNormalizer.trim(
                        request.name()
                );

        String normalizedDescription =
                TextNormalizer.trimToNull(
                        request.description()
                );

        if (projectRepository
                .existsByCodeIgnoreCase(
                        normalizedCode
                )) {

            throw new BusinessException(
                    ErrorCode.PROJECT_CODE_ALREADY_EXISTS
            );
        }

        Project project = Project.builder()
                .code(normalizedCode)
                .name(normalizedName)
                .description(normalizedDescription)
                .status(ProjectStatus.PLANNING)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .createdByUserId(
                        currentUser.getId()
                )
                .build();

        Project savedProject =
                projectRepository.save(project);

        ProjectMember owner =
                ProjectMember.builder()
                        .projectId(
                                savedProject.getId()
                        )
                        .userId(
                                currentUser.getId()
                        )
                        .role(
                                ProjectMemberRole.OWNER
                        )
                        .joinedAt(
                                Instant.now()
                        )
                        .build();

        projectMemberRepository.save(owner);

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "code",
                savedProject.getCode()
        );

        newValue.put(
                "name",
                savedProject.getName()
        );

        newValue.put(
                "status",
                savedProject.getStatus()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        savedProject.getId(),
                        ActivityEntityType.PROJECT,
                        savedProject.getId(),
                        ProjectActivityAction.PROJECT_CREATED,
                        currentUser.getId(),
                        null,
                        newValue
                )
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction.CREATE_PROJECT.name()
        );

        return projectMapper.toResponse(
                savedProject,
                ProjectMemberRole.OWNER
        );
    }

    // ===================== SEARCH PROJECT =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.PROJECT_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() " +
                    "+ '|request=' + (#request == null ? '' : #request.toString()) " +
                    "+ '|page=' + #pageable.pageNumber " +
                    "+ '|size=' + #pageable.pageSize " +
                    "+ '|sort=' + #pageable.sort.toString()"
    )
    public ProjectPageResponse searchProjects(
            ProjectSearchRequest request,
            Pageable pageable
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        String keyword =
                request != null
                        ? request.keyword()
                        : null;

        ProjectStatus status =
                request != null
                        ? request.status()
                        : null;

        DateRangeValidator.validate(
                request == null ? null : request.startDateFrom(),
                request == null ? null : request.startDateTo()
        );
        DateRangeValidator.validate(
                request == null ? null : request.endDateFrom(),
                request == null ? null : request.endDateTo()
        );
        DateRangeValidator.validate(
                request == null ? null : request.createdFrom(),
                request == null ? null : request.createdTo()
        );
        PageableValidator.validate(
                pageable,
                Set.of(
                        "createdAt",
                        "updatedAt",
                        "code",
                        "name",
                        "status",
                        "startDate",
                        "endDate"
                )
        );

        Specification<Project> specification =
                Specification.allOf(
                        ProjectSpecification.search(
                                keyword
                        ),
                        ProjectSpecification.hasStatus(
                                status
                        ),
                        ProjectSpecification.startDateBetween(
                                request == null ? null : request.startDateFrom(),
                                request == null ? null : request.startDateTo()
                        ),
                        ProjectSpecification.endDateBetween(
                                request == null ? null : request.endDateFrom(),
                                request == null ? null : request.endDateTo()
                        ),
                        ProjectSpecification.createdAtBetween(
                                request == null ? null : request.createdFrom(),
                                request == null ? null : request.createdTo()
                        )
                );

        if (currentUser.getRole()
                != UserRole.ADMIN) {

            List<UUID> projectIds =
                    projectMemberRepository
                            .findProjectIdsByUserId(
                                    currentUser.getId()
                            );

            specification =
                    specification.and(
                            ProjectSpecification.idIn(
                                    projectIds
                            )
                    );
        }

        Page<ProjectResponse> responsePage =
                projectRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(project -> {

                            ProjectMemberRole currentUserRole =
                                    resolveCurrentUserRole(
                                            project.getId(),
                                            currentUser
                                    );

                            return projectMapper.toResponse(
                                    project,
                                    currentUserRole
                            );
                        });

        return ProjectPageResponse.from(
                responsePage
        );
    }

    // ===================== PROJECT DETAIL =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.PROJECT_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() " +
                    "+ ':' + #projectId"
    )
    public ProjectResponse getProjectById(
            UUID projectId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireViewAccess(
                        project,
                        currentUser
                );

        ProjectMemberRole currentUserRole =
                resolveCurrentUserRole(
                        projectId,
                        currentUser
                );

        return projectMapper.toResponse(
                project,
                currentUserRole
        );
    }

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
            ),
            @CacheEvict(
                    value = CacheNames.MY_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireProjectUpdateAccess(
                        projectId,
                        currentUser
                );

        ProjectValidator.validateEditable(
                project
        );

        /*
         * Phải lưu dữ liệu cũ trước khi thay đổi entity.
         */
        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "name",
                project.getName()
        );

        oldValue.put(
                "description",
                project.getDescription()
        );

        oldValue.put(
                "startDate",
                project.getStartDate()
        );

        oldValue.put(
                "endDate",
                project.getEndDate()
        );

        LocalDate newStartDate =
                request.startDate() != null
                        ? request.startDate()
                        : project.getStartDate();

        LocalDate newEndDate =
                request.endDate() != null
                        ? request.endDate()
                        : project.getEndDate();

        ProjectValidator.validateDates(
                newStartDate,
                newEndDate
        );

        if (request.name() != null) {
            String normalizedName =
                    TextNormalizer.trim(
                            request.name()
                    );

            /*
             * request.name() đã khác null,
             * nên normalizedName không thể null.
             */
            if (normalizedName.isBlank()) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR
                );
            }

            project.setName(
                    normalizedName
            );
        }

        if (request.description() != null) {
            project.setDescription(
                    TextNormalizer.trimToNull(
                            request.description()
                    )
            );
        }

        if (request.startDate() != null) {
            project.setStartDate(
                    request.startDate()
            );
        }

        if (request.endDate() != null) {
            project.setEndDate(
                    request.endDate()
            );
        }

        Project savedProject =
                projectRepository.save(project);

        Map<String, Object> newValue =
                new LinkedHashMap<>();

        newValue.put(
                "name",
                savedProject.getName()
        );

        newValue.put(
                "description",
                savedProject.getDescription()
        );

        newValue.put(
                "startDate",
                savedProject.getStartDate()
        );

        newValue.put(
                "endDate",
                savedProject.getEndDate()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        ProjectActivityAction.PROJECT_UPDATED,
                        currentUser.getId(),
                        oldValue,
                        newValue
                )
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction.UPDATE_PROJECT.name()
        );

        ProjectMemberRole currentUserRole =
                resolveCurrentUserRole(
                        projectId,
                        currentUser
                );

        return projectMapper.toResponse(
                savedProject,
                currentUserRole
        );
    }

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
            ),
            @CacheEvict(
                    value = CacheNames.MY_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public ProjectResponse updateProjectStatus(
            UUID projectId,
            UpdateProjectStatusRequest request
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(projectId);

        projectAccessService
                .requireProjectUpdateAccess(
                        projectId,
                        currentUser
                );

        ProjectValidator.validateEditable(
                project
        );
        
        ProjectStatus currentStatus =
                project.getStatus();

        ProjectStatus newStatus =
                request.status();

        ProjectValidator
                .validateStatusTransition(
                        currentStatus,
                        newStatus
                );

        project.setStatus(
                newStatus
        );

        Project savedProject =
                projectRepository.save(project);

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        ProjectActivityAction.PROJECT_STATUS_CHANGED,
                        currentUser.getId(),
                        Map.of(
                                "status",
                                currentStatus
                        ),
                        Map.of(
                                "status",
                                newStatus
                        )
                )
        );

        List<UUID> recipientIds =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        )
                        .stream()
                        .map(ProjectMember::getUserId)
                        .distinct()
                        .toList();

        notificationService.create(
                new NotificationCommand(
                        NotificationType.PROJECT_STATUS_CHANGED,
                        "Trạng thái dự án đã thay đổi",
                        "Dự án "
                                + savedProject.getName()
                                + " đã chuyển từ "
                                + currentStatus
                                + " sang "
                                + newStatus,
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        recipientIds
                )
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction
                        .PROJECT_STATUS_CHANGED
                        .name()
        );

        ProjectMemberRole currentUserRole =
                resolveCurrentUserRole(
                        projectId,
                        currentUser
                );

        return projectMapper.toResponse(
                savedProject,
                currentUserRole
        );
    }

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
            ),
            @CacheEvict(
                    value = CacheNames.MY_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public void deleteProject(
            UUID projectId
    ) {
        User currentUser =
                currentUserService
                        .getActiveCurrentUser();

        Project project =
                projectAccessService
                        .getProjectOrThrow(
                                projectId
                        );

        projectAccessService
                .requireProjectDeleteAccess(
                        projectId,
                        currentUser
                );

        List<UUID> recipientIds =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(
                                projectId
                        )
                        .stream()
                        .map(ProjectMember::getUserId)
                        .distinct()
                        .toList();

        Map<String, Object> oldValue =
                new LinkedHashMap<>();

        oldValue.put(
                "name",
                project.getName()
        );

        oldValue.put(
                "status",
                project.getStatus()
        );

        projectActivityService.log(
                new ProjectActivityCommand(
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        ProjectActivityAction.PROJECT_DELETED,
                        currentUser.getId(),
                        oldValue,
                        Map.of(
                                "deleted",
                                true
                        )
                )
        );

        notificationService.create(
                new NotificationCommand(
                        NotificationType.PROJECT_DELETED,
                        "Dự án đã bị xóa",
                        "Dự án "
                                + project.getName()
                                + " đã bị xóa",
                        currentUser.getId(),
                        projectId,
                        ActivityEntityType.PROJECT,
                        projectId,
                        recipientIds
                )
        );

        project.markDeleted(
                currentUser.getUsername()
        );

        projectRepository.save(project);

        softDeleteProjectMembers(
                projectId,
                currentUser.getUsername()
        );

        auditLogService.log(
                currentUser.getId(),
                AuditAction.DELETE_PROJECT.name()
        );
    }
    // ===================== HELPER =====================

    private ProjectMemberRole resolveCurrentUserRole(
            UUID projectId,
            User currentUser
    ) {
        /*
         * ADMIN được quyền xem phục vụ quản trị,
         * nhưng không phải thành viên dự án.
         */
        if (currentUser.getRole()
                == UserRole.ADMIN) {
            return null;
        }

        return projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        currentUser.getId()
                )
                .map(ProjectMember::getRole)
                .orElse(null);
    }

    private void softDeleteProjectMembers(
            UUID projectId,
            String deletedBy
    ) {
        projectMemberRepository
                .softDeleteAllByProjectId(
                        projectId,
                        Instant.now(),
                        deletedBy
                );
    }
}
