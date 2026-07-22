package com.project.taskmanagement.dto.response.search;

import com.project.taskmanagement.enums.SearchEntityType;

import java.time.Instant;
import java.util.UUID;

public record SearchResultItemResponse(
        SearchEntityType entityType,
        UUID entityId,
        String title,
        String description,
        UUID projectId,
        String projectCode,
        String projectName,
        String targetUrl,
        String matchedText,
        Instant updatedAt
) {
}
