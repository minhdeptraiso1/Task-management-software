package com.project.taskmanagement.dto.response.search;

import java.util.List;

public record GlobalSearchResponse(
        String keyword,
        long totalElements,
        List<SearchResultItemResponse> results
) {
}
