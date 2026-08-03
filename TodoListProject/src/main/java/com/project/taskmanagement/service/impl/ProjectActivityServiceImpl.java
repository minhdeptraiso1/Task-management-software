package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.project.ProjectActivitySearchRequest;
import com.project.taskmanagement.dto.response.project.ProjectActivityDetailResponse;
import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectActivityResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.repository.spec.ProjectActivitySpecification;
import com.project.taskmanagement.service.ProjectActivityDisplayService;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.helper.UserLookupHelper;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectActivityServiceImpl
        implements ProjectActivityService {

    ProjectActivityLogRepository repository;
    UserLookupHelper userLookupHelper;

    ObjectMapper objectMapper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    ProjectActivityDisplayService
            projectActivityDisplayService;

    // ===================== CREATE LOG =====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PROJECT_ACTIVITY_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY, allEntries = true),
            @CacheEvict(value = CacheNames.PROJECT_DASHBOARD, allEntries = true)
    })
    public void log(
            ProjectActivityCommand command
    ) {
        if (command == null) {
            return;
        }

        ProjectActivityLog activityLog =
                ProjectActivityLog.builder()
                        .projectId(
                                command.projectId()
                        )
                        .entityType(
                                command.entityType()
                        )
                        .entityId(
                                command.entityId()
                        )
                        .action(
                                command.action()
                        )
                        .performedByUserId(
                                command.performedByUserId()
                        )
                        .oldValueJson(
                                sanitizeAndSerialize(
                                        command.oldValue()
                                )
                        )
                        .newValueJson(
                                sanitizeAndSerialize(
                                        command.newValue()
                                )
                        )
                        .build();

        repository.save(activityLog);
    }

    // ===================== GET ACTIVITIES =====================

    @Override
    @Transactional(readOnly = true)
    public ProjectActivityPageResponse getActivities(
            UUID projectId,
            ProjectActivitySearchRequest request,
            Pageable pageable
    ) {
        requireViewAccess(
                projectId
        );

        ProjectActivitySearchRequest safeRequest =
                request == null
                        ? emptyRequest()
                        : request;

        Specification<ProjectActivityLog> specification =
                buildSpecification(
                        projectId,
                        null,
                        null,
                        safeRequest
                );

        Page<ProjectActivityLog> activityPage =
                repository.findAll(
                        specification,
                        pageable
                );

        return toPageResponse(
                activityPage
        );
    }

    // ===================== GET DETAIL =====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PROJECT_ACTIVITY_DETAIL,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + ':' + #projectId" +
                    " + ':' + #activityId"
    )
    public ProjectActivityDetailResponse getActivityById(
            UUID projectId,
            UUID activityId
    ) {
        requireViewAccess(
                projectId
        );

        ProjectActivityLog activity =
                repository
                        .findByIdAndProjectId(
                                activityId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .PROJECT_ACTIVITY_NOT_FOUND
                                )
                        );

        User performer = userLookupHelper.getOrNull(
                userLookupHelper.findUserMap(
                        Collections.singletonList(activity.getPerformedByUserId())
                ),
                activity.getPerformedByUserId()
        );

        String username =
                performer == null
                        ? "Người dùng không tồn tại"
                        : performer.getUsername();

        String email =
                performer == null
                        ? null
                        : performer.getEmail();

        return new ProjectActivityDetailResponse(
                activity.getId(),
                activity.getProjectId(),
                activity.getEntityType(),
                activity.getEntityId(),
                activity.getAction(),
                activity.getPerformedByUserId(),
                username,
                email,
                activity.getOldValueJson(),
                activity.getNewValueJson(),
                projectActivityDisplayService
                        .resolveDisplayMessage(
                                activity,
                                username
                        ),
                activity.getCreatedAt()
        );
    }

    // ===================== GET ENTITY ACTIVITIES =====================

    @Override
    @Transactional(readOnly = true)
    public ProjectActivityPageResponse getEntityActivities(
            UUID projectId,
            ActivityEntityType entityType,
            UUID entityId,
            ProjectActivitySearchRequest request,
            Pageable pageable
    ) {
        requireViewAccess(
                projectId
        );

        ProjectActivitySearchRequest safeRequest =
                request == null
                        ? emptyRequest()
                        : request;

        Specification<ProjectActivityLog> specification =
                buildSpecification(
                        projectId,
                        entityType,
                        entityId,
                        safeRequest
                );

        Page<ProjectActivityLog> activityPage =
                repository.findAll(
                        specification,
                        pageable
                );

        return toPageResponse(
                activityPage
        );
    }

    // ===================== ACCESS =====================

    private void requireViewAccess(
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
    }

    // ===================== SPECIFICATION =====================

    private Specification<ProjectActivityLog>
    buildSpecification(
            UUID projectId,
            ActivityEntityType fixedEntityType,
            UUID fixedEntityId,
            ProjectActivitySearchRequest request
    ) {
        ActivityEntityType entityType =
                fixedEntityType != null
                        ? fixedEntityType
                        : request.entityType();

        return Specification.allOf(
                ProjectActivitySpecification
                        .hasProjectId(
                                projectId
                        ),
                ProjectActivitySpecification
                        .hasEntityType(
                                entityType
                        ),
                ProjectActivitySpecification
                        .hasEntityId(
                                fixedEntityId
                        ),
                ProjectActivitySpecification
                        .hasAction(
                                request.action()
                        ),
                ProjectActivitySpecification
                        .hasPerformedByUserId(
                                request.performedByUserId()
                        ),
                ProjectActivitySpecification
                        .createdFrom(
                                request.fromDate()
                        ),
                ProjectActivitySpecification
                        .createdTo(
                                request.toDate()
                        ),
                ProjectActivitySpecification
                        .searchKeyword(
                                request.keyword()
                        )
        );
    }

    private ProjectActivitySearchRequest
    emptyRequest() {
        return new ProjectActivitySearchRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    // ===================== PAGE MAPPER =====================

    private ProjectActivityPageResponse toPageResponse(
            Page<ProjectActivityLog> activityPage
    ) {
        List<ProjectActivityLog> activities =
                activityPage.getContent();

        Map<UUID, User> usersById = userLookupHelper.findUserMap(
                activities.stream()
                        .map(ProjectActivityLog::getPerformedByUserId)
                        .toList()
        );

        Page<ProjectActivityResponse> responsePage =
                activityPage.map(activity -> {

                    User performer =
                            usersById.get(
                                    activity
                                            .getPerformedByUserId()
                            );

                    String username =
                            performer == null
                                    ? "Người dùng không tồn tại"
                                    : performer.getUsername();

                    String email =
                            performer == null
                                    ? null
                                    : performer.getEmail();

                    return toResponse(
                            activity,
                            username,
                            email
                    );
                });

        return ProjectActivityPageResponse.from(
                responsePage
        );
    }

    // ===================== MAPPER =====================

    private ProjectActivityResponse toResponse(
            ProjectActivityLog activity,
            String performerUsername,
            String performerEmail
    ) {
        return new ProjectActivityResponse(
                activity.getId(),
                activity.getProjectId(),
                activity.getEntityType(),
                activity.getEntityId(),
                activity.getAction(),
                activity.getPerformedByUserId(),
                performerUsername,
                performerEmail,
                activity.getOldValueJson(),
                activity.getNewValueJson(),
                projectActivityDisplayService
                        .resolveDisplayMessage(
                                activity,
                                performerUsername
                        ),
                activity.getCreatedAt()
        );
    }

    // ===================== SERIALIZE =====================

    private String sanitizeAndSerialize(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        Object sanitizedValue =
                sanitizeValue(
                        value
                );

        try {
            return objectMapper
                    .writeValueAsString(
                            sanitizedValue
                    );

        } catch (JsonProcessingException exception) {
            return String.valueOf(
                    sanitizedValue
            );
        }
    }

    private Object sanitizeValue(
            Object value
    ) {
        if (!(value instanceof Map<?, ?> sourceMap)) {
            return value;
        }

        Map<String, Object> sanitized =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry
                : sourceMap.entrySet()) {

            if (entry.getKey() == null) {
                continue;
            }

            String key =
                    String.valueOf(
                            entry.getKey()
                    );

            if (isSensitiveKey(key)) {
                continue;
            }

            sanitized.put(
                    key,
                    entry.getValue()
            );
        }

        return sanitized;
    }

    private boolean isSensitiveKey(
            String key
    ) {
        if (key == null) {
            return false;
        }

        String normalized =
                key.toLowerCase();

        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization");
    }
}
