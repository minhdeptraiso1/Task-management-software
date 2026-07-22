package com.project.taskmanagement.repository.projection.search;

import java.time.Instant;
import java.util.UUID;

public interface SearchResultView {

    String getEntityType();

    UUID getEntityId();

    String getTitle();

    String getDescription();

    UUID getProjectId();

    String getProjectCode();

    String getProjectName();

    Instant getUpdatedAt();
}
