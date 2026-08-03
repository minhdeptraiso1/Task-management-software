package com.project.taskmanagement.dto.request.search;

import com.project.taskmanagement.enums.SearchEntityType;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GlobalSearchRequest(
        @Size(max = 100, message = "Từ khóa tìm kiếm không được vượt quá 100 ký tự")
        String q,
        SearchEntityType entityType,
        UUID projectId
) {
}
