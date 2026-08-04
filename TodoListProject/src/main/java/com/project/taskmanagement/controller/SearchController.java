package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.search.GlobalSearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.search.GlobalSearchResponse;
import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.service.SearchService;
import com.project.taskmanagement.service.context.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.SEARCH, description = "Tìm kiếm toàn hệ thống và trong phạm vi Project theo quyền")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {

    SearchService searchService;
    RateLimitService rateLimitService;
    CurrentUserService currentUserService;

    @Operation(summary = "Search toàn hệ thống theo quyền người dùng")
    @GetMapping("/search")
    public ApiResponseSever<GlobalSearchResponse> search(
            @Valid
            @ParameterObject GlobalSearchRequest request
    ) {
        rateLimitService.check(
                RateLimitAction.GLOBAL_SEARCH,
                currentUserService
                        .getActiveCurrentUser()
                        .getId()
                        .toString()
        );

        return ApiResponseSever.ok(searchService.search(request));
    }

    @Operation(summary = "Search trong một Project")
    @GetMapping("/projects/{projectId}/search")
    public ApiResponseSever<GlobalSearchResponse> searchInProject(
            @PathVariable UUID projectId,
            @Valid
            @ParameterObject GlobalSearchRequest request
    ) {
        String currentUserId =
                currentUserService
                        .getActiveCurrentUser()
                        .getId()
                        .toString();

        rateLimitService.check(
                RateLimitAction.PROJECT_SEARCH,
                currentUserId + ":" + projectId
        );

        return ApiResponseSever.ok(searchService.searchInProject(projectId, request));
    }
}
