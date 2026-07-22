package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.search.GlobalSearchRequest;
import com.project.taskmanagement.dto.response.search.GlobalSearchResponse;

import java.util.UUID;

public interface SearchService {

    GlobalSearchResponse search(GlobalSearchRequest request);

    GlobalSearchResponse searchInProject(UUID projectId, GlobalSearchRequest request);
}
