package com.project.taskmanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectActivityResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.ProjectActivityLogRepository;
import com.project.taskmanagement.service.ProjectActivityService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectActivityServiceImpl
        implements ProjectActivityService {

    ProjectActivityLogRepository repository;
    ObjectMapper objectMapper;

    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    // ===================== CREATE LOG =====================

    @Override
    @Transactional
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
                                serialize(
                                        command.oldValue()
                                )
                        )
                        .newValueJson(
                                serialize(
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
            Pageable pageable
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

        Page<ProjectActivityResponse> responsePage =
                repository
                        .findAllByProjectIdOrderByCreatedAtDesc(
                                projectId,
                                pageable
                        )
                        .map(this::toResponse);

        return ProjectActivityPageResponse.from(
                responsePage
        );
    }

    // ===================== MAPPER =====================

    private ProjectActivityResponse toResponse(
            ProjectActivityLog activityLog
    ) {
        return new ProjectActivityResponse(
                activityLog.getId(),
                activityLog.getProjectId(),
                activityLog.getEntityType(),
                activityLog.getEntityId(),
                activityLog.getAction(),
                activityLog.getPerformedByUserId(),
                activityLog.getOldValueJson(),
                activityLog.getNewValueJson(),
                activityLog.getCreatedAt()
        );
    }

    // ===================== SERIALIZE =====================

    private String serialize(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper
                    .writeValueAsString(value);

        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }
}