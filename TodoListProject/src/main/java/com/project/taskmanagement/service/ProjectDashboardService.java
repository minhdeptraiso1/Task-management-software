package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardActivityResponse;
import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardMemberWorkloadResponse;
import com.project.taskmanagement.dto.response.dashboard.ProjectDashboardResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectDashboardService {

    ProjectDashboardResponse getDashboard(
            UUID projectId
    );

    List<ProjectDashboardMemberWorkloadResponse>
    getWorkload(
            UUID projectId
    );

    List<ProjectDashboardActivityResponse>
    getRecentActivities(
            UUID projectId,
            int limit
    );
}