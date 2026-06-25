package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.project.ProjectActivityPageResponse;
import com.project.taskmanagement.service.model.ProjectActivityCommand;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectActivityService {

    void log(
            ProjectActivityCommand command
    );

    ProjectActivityPageResponse getActivities(
            UUID projectId,
            Pageable pageable
    );
}