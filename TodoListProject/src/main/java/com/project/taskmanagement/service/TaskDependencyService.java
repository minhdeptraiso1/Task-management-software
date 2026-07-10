package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.task.BlockTaskRequest;
import com.project.taskmanagement.dto.request.task.CreateTaskDependencyRequest;
import com.project.taskmanagement.dto.request.task.UnblockTaskRequest;
import com.project.taskmanagement.dto.response.task.ProjectTaskRiskPageResponse;
import com.project.taskmanagement.dto.response.task.TaskDependencyResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.dto.response.task.TaskRiskResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskDependencyService {

    TaskDependencyResponse addDependency(
            UUID projectId,
            UUID taskId,
            CreateTaskDependencyRequest request
    );

    List<TaskDependencyResponse> getDependencies(
            UUID projectId,
            UUID taskId
    );

    void removeDependency(
            UUID projectId,
            UUID taskId,
            UUID dependencyId
    );

    TaskResponse blockTask(
            UUID projectId,
            UUID taskId,
            BlockTaskRequest request
    );

    TaskResponse unblockTask(
            UUID projectId,
            UUID taskId,
            UnblockTaskRequest request
    );

    TaskRiskResponse getTaskRisk(
            UUID projectId,
            UUID taskId
    );

    ProjectTaskRiskPageResponse getProjectTaskRisks(
            UUID projectId,
            Pageable pageable
    );
}
