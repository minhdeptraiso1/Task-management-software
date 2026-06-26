package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.sprint.CreateSprintRequest;
import com.project.taskmanagement.dto.request.sprint.SprintSearchRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintRequest;
import com.project.taskmanagement.dto.response.backlog.BacklogItemResponse;
import com.project.taskmanagement.dto.response.sprint.SprintPageResponse;
import com.project.taskmanagement.dto.response.sprint.SprintResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SprintService {

    SprintResponse create(
            UUID projectId,
            CreateSprintRequest request
    );

    SprintPageResponse search(
            UUID projectId,
            SprintSearchRequest request,
            Pageable pageable
    );

    SprintResponse getById(
            UUID projectId,
            UUID sprintId
    );

    SprintResponse update(
            UUID projectId,
            UUID sprintId,
            UpdateSprintRequest request
    );

    void delete(
            UUID projectId,
            UUID sprintId
    );

    BacklogItemResponse addBacklogItem(
            UUID projectId,
            UUID sprintId,
            UUID itemId
    );

    BacklogItemResponse removeBacklogItem(
            UUID projectId,
            UUID sprintId,
            UUID itemId
    );

    SprintResponse start(
            UUID projectId,
            UUID sprintId
    );

    SprintResponse complete(
            UUID projectId,
            UUID sprintId
    );

    SprintResponse cancel(
            UUID projectId,
            UUID sprintId
    );
}