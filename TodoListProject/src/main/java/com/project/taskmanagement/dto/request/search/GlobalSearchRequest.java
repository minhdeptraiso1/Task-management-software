package com.project.taskmanagement.dto.request.search;

import com.project.taskmanagement.enums.SearchEntityType;

import java.util.UUID;

public record GlobalSearchRequest(
        String q,
        SearchEntityType entityType,
        UUID projectId
) {
}
