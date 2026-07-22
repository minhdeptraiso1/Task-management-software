package com.project.taskmanagement.repository.projection.search;

import java.time.Instant;
import java.util.UUID;

public record SearchResultViewImpl(
        String entityType,
        UUID entityId,
        String title,
        String description,
        UUID projectId,
        String projectCode,
        String projectName,
        Instant updatedAt
) implements SearchResultView {

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public String getProjectCode() {
        return projectCode;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
