package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.backlog.*;
import com.project.taskmanagement.dto.response.backlog.BacklogItemPageResponse;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BacklogItemService {

    BacklogItemResponse create(
            UUID projectId,
            CreateBacklogItemRequest request
    );

    BacklogItemPageResponse search(
            UUID projectId,
            BacklogSearchRequest request,
            Pageable pageable
    );

    BacklogItemResponse getById(
            UUID projectId,
            UUID itemId
    );

    BacklogItemResponse update(
            UUID projectId,
            UUID itemId,
            UpdateBacklogItemRequest request
    );

    BacklogItemResponse updateStatus(
            UUID projectId,
            UUID itemId,
            UpdateBacklogItemStatusRequest request
    );

    BacklogItemResponse updatePriority(
            UUID projectId,
            UUID itemId,
            UpdateBacklogPriorityRequest request
    );

    void delete(
            UUID projectId,
            UUID itemId
    );

    BacklogItemResponse updatePosition(
            UUID projectId,
            UUID itemId,
            UpdateBacklogPositionRequest request
    );
}