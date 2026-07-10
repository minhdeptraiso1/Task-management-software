package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.task.*;
import com.project.taskmanagement.dto.response.kanban.KanbanBoardResponse;
import com.project.taskmanagement.dto.response.task.TaskPageResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponse create(
            UUID projectId,
            CreateTaskRequest request
    );

    TaskPageResponse search(
            UUID projectId,
            TaskSearchRequest request,
            Pageable pageable
    );

    TaskResponse getById(
            UUID projectId,
            UUID taskId
    );

    TaskResponse update(
            UUID projectId,
            UUID taskId,
            UpdateTaskRequest request
    );

    TaskResponse assign(
            UUID projectId,
            UUID taskId,
            AssignTaskRequest request
    );

    TaskResponse unassign(
            UUID projectId,
            UUID taskId
    );

    void delete(
            UUID projectId,
            UUID taskId
    );

    KanbanBoardResponse getKanbanBoard(
            UUID projectId,
            UUID sprintId
    );

    TaskResponse updateStatus(
            UUID projectId,
            UUID taskId,
            UpdateTaskStatusRequest request
    );

    TaskResponse block(
            UUID projectId,
            UUID taskId,
            BlockTaskRequest request
    );

    TaskResponse reopen(
            UUID projectId,
            UUID taskId,
            ReopenTaskRequest request
    );

    TaskResponse updatePosition(
            UUID projectId,
            UUID taskId,
            UpdateTaskPositionRequest request
    );
}
