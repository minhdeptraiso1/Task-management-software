package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.ProjectActivityLog;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ProjectActivityDisplayService {

    Map<UUID, String> resolveUsernames(
            Collection<ProjectActivityLog> activities
    );

    String resolveDisplayMessage(
            ProjectActivityLog activity,
            String performerName
    );
}