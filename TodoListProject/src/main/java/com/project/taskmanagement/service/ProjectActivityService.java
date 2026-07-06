package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.project.ProjectActivitySearchRequest;
import com.project.taskmanagement.dto.response.project.ProjectActivityDetailResponse;
import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectActivityService {

    void log(
            ProjectActivityCommand command
    );

    ProjectActivityPageResponse getActivities(
            UUID projectId,
            ProjectActivitySearchRequest request,
            Pageable pageable
    );

    ProjectActivityDetailResponse getActivityById(
            UUID projectId,
            UUID activityId
    );

    ProjectActivityPageResponse getEntityActivities(
            UUID projectId,
            ActivityEntityType entityType,
            UUID entityId,
            ProjectActivitySearchRequest request,
            Pageable pageable
    );
}