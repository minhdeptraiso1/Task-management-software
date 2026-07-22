package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.search.GlobalSearchRequest;
import com.project.taskmanagement.dto.response.search.GlobalSearchResponse;
import com.project.taskmanagement.dto.response.search.SearchResultItemResponse;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SearchEntityType;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.SearchRepository;
import com.project.taskmanagement.repository.projection.search.SearchResultView;
import com.project.taskmanagement.service.SearchService;
import com.project.taskmanagement.service.access.ProjectAccessService;
import com.project.taskmanagement.service.context.CurrentUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchServiceImpl implements SearchService {

    static final int DEFAULT_LIMIT = 30;
    static final int MAX_LIMIT = 50;

    SearchRepository searchRepository;
    ProjectMemberRepository projectMemberRepository;
    CurrentUserService currentUserService;
    ProjectAccessService projectAccessService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.GLOBAL_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|scope=global'" +
                    " + '|q=' + (#request == null || #request.q() == null ? '' : #request.q())" +
                    " + '|type=' + (#request == null || #request.entityType() == null ? '' : #request.entityType())" +
                    " + '|project=' + (#request == null || #request.projectId() == null ? '' : #request.projectId())"
    )
    public GlobalSearchResponse search(GlobalSearchRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        String keyword = normalizeKeyword(request == null ? null : request.q());
        if (keyword.isBlank()) {
            return new GlobalSearchResponse(keyword, 0, List.of());
        }

        SearchEntityType entityType = request == null ? null : request.entityType();
        UUID projectId = request == null ? null : request.projectId();
        boolean admin = currentUser.getRole() == UserRole.ADMIN;
        List<UUID> allowedProjectIds = admin
                ? List.of()
                : projectMemberRepository.findProjectIdsByUserId(currentUser.getId());

        if (projectId != null) {
            Project project = projectAccessService.getProjectOrThrow(projectId);
            projectAccessService.requireViewAccess(project, currentUser);
        }

        List<SearchResultItemResponse> results = searchRepository
                .search(keyword, entityType, projectId, allowedProjectIds, admin, MAX_LIMIT)
                .stream()
                .map(view -> toResponse(view, keyword))
                .toList();

        return new GlobalSearchResponse(keyword, results.size(), results);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.GLOBAL_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username()" +
                    " + '|scope=project'" +
                    " + '|project=' + #projectId" +
                    " + '|q=' + (#request == null || #request.q() == null ? '' : #request.q())" +
                    " + '|type=' + (#request == null || #request.entityType() == null ? '' : #request.entityType())"
    )
    public GlobalSearchResponse searchInProject(UUID projectId, GlobalSearchRequest request) {
        User currentUser = currentUserService.getActiveCurrentUser();
        Project project = projectAccessService.getProjectOrThrow(projectId);
        projectAccessService.requireViewAccess(project, currentUser);

        String keyword = normalizeKeyword(request == null ? null : request.q());
        if (keyword.isBlank()) {
            return new GlobalSearchResponse(keyword, 0, List.of());
        }

        SearchEntityType entityType = request == null ? null : request.entityType();
        boolean admin = currentUser.getRole() == UserRole.ADMIN;
        List<UUID> allowedProjectIds = admin ? List.of() : List.of(projectId);

        List<SearchResultItemResponse> results = searchRepository
                .search(keyword, entityType, projectId, allowedProjectIds, admin, DEFAULT_LIMIT)
                .stream()
                .map(view -> toResponse(view, keyword))
                .toList();

        return new GlobalSearchResponse(keyword, results.size(), results);
    }

    private SearchResultItemResponse toResponse(SearchResultView view, String keyword) {
        SearchEntityType entityType = SearchEntityType.valueOf(view.getEntityType());

        return new SearchResultItemResponse(
                entityType,
                view.getEntityId(),
                view.getTitle(),
                view.getDescription(),
                view.getProjectId(),
                view.getProjectCode(),
                view.getProjectName(),
                buildTargetUrl(entityType, view),
                buildMatchedText(view, keyword),
                view.getUpdatedAt()
        );
    }

    private String buildTargetUrl(SearchEntityType entityType, SearchResultView view) {
        UUID projectId = view.getProjectId();
        UUID entityId = view.getEntityId();

        return switch (entityType) {
            case PROJECT -> "/projects/" + projectId;
            case SPRINT -> "/projects/" + projectId + "/sprints/" + entityId;
            case BACKLOG_ITEM -> "/projects/" + projectId + "/backlog-items/" + entityId;
            case TASK -> "/projects/" + projectId + "/tasks/" + entityId;
            case BUG -> "/projects/" + projectId + "/bugs/" + entityId;
            case COMMENT -> "/projects/" + projectId + "/comments/" + entityId;
            case ATTACHMENT -> "/projects/" + projectId + "/attachments/" + entityId + "/download";
        };
    }

    private String buildMatchedText(SearchResultView view, String keyword) {
        String source = firstNotBlank(view.getTitle(), view.getDescription());
        if (source.isBlank()) {
            return "";
        }

        String lowerSource = source.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = lowerSource.indexOf(lowerKeyword);

        if (index < 0) {
            return source.length() <= 120 ? source : source.substring(0, 120);
        }

        int start = Math.max(0, index - 40);
        int end = Math.min(source.length(), index + keyword.length() + 80);
        return source.substring(start, end);
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
