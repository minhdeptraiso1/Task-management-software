package com.project.taskmanagement.repository;

import com.project.taskmanagement.enums.SearchEntityType;
import com.project.taskmanagement.repository.projection.search.SearchResultView;

import java.util.List;
import java.util.UUID;

public interface SearchRepository {

    List<SearchResultView> search(
            String keyword,
            SearchEntityType entityType,
            UUID projectId,
            List<UUID> allowedProjectIds,
            boolean admin,
            int limit
    );
}
