package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.search.GlobalSearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.search.GlobalSearchResponse;
import com.project.taskmanagement.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {

    SearchService searchService;

    @Operation(summary = "Search toàn hệ thống theo quyền người dùng")
    @GetMapping("/search")
    public ApiResponseSever<GlobalSearchResponse> search(
            @ParameterObject GlobalSearchRequest request
    ) {
        return ApiResponseSever.ok(searchService.search(request));
    }

    @Operation(summary = "Search trong một Project")
    @GetMapping("/projects/{projectId}/search")
    public ApiResponseSever<GlobalSearchResponse> searchInProject(
            @PathVariable UUID projectId,
            @ParameterObject GlobalSearchRequest request
    ) {
        return ApiResponseSever.ok(searchService.searchInProject(projectId, request));
    }
}
