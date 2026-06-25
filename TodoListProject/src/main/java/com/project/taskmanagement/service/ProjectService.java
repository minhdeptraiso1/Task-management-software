package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.project.CreateProjectRequest;
import com.project.taskmanagement.dto.request.project.ProjectSearchRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectRequest;
import com.project.taskmanagement.dto.request.project.UpdateProjectStatusRequest;
import com.project.taskmanagement.dto.response.project.ProjectPageResponse;
import com.project.taskmanagement.dto.response.project.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(
            CreateProjectRequest request
    );

    ProjectPageResponse searchProjects(
            ProjectSearchRequest request,
            Pageable pageable
    );

    ProjectResponse getProjectById(
            UUID projectId
    );

    ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request
    );

    ProjectResponse updateProjectStatus(
            UUID projectId,
            UpdateProjectStatusRequest request
    );

    void deleteProject(
            UUID projectId
    );
}