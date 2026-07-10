package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.task.CreateTaskDependencyRequest;
import com.project.taskmanagement.dto.request.task.UnblockTaskRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.task.TaskDependencyResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.dto.response.task.TaskRiskResponse;
import com.project.taskmanagement.service.TaskDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskDependencyController {

    TaskDependencyService taskDependencyService;

    @Operation(
            summary = "Add Task dependency"
    )
    @PostMapping("/tasks/{taskId}/dependencies")
    public ResponseEntity<ApiResponseSever<TaskDependencyResponse>>
    addDependency(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            CreateTaskDependencyRequest request
    ) {
        TaskDependencyResponse response =
                taskDependencyService.addDependency(
                        projectId,
                        taskId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseSever.ok(response));
    }

    @Operation(
            summary = "Get Task dependencies"
    )
    @GetMapping("/tasks/{taskId}/dependencies")
    public ApiResponseSever<List<TaskDependencyResponse>>
    getDependencies(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        return ApiResponseSever.ok(
                taskDependencyService.getDependencies(
                        projectId,
                        taskId
                )
        );
    }

    @Operation(
            summary = "Remove Task dependency"
    )
    @DeleteMapping("/tasks/{taskId}/dependencies/{dependencyId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @PathVariable
            UUID dependencyId
    ) {
        taskDependencyService.removeDependency(
                projectId,
                taskId,
                dependencyId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Unblock Task"
    )
    @PatchMapping("/tasks/{taskId}/unblock")
    public ApiResponseSever<TaskResponse>
    unblockTask(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @RequestBody(required = false)
            UnblockTaskRequest request
    ) {
        return ApiResponseSever.ok(
                taskDependencyService.unblockTask(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Get Task risk"
    )
    @GetMapping("/tasks/{taskId}/risk")
    public ApiResponseSever<TaskRiskResponse>
    getTaskRisk(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        return ApiResponseSever.ok(
                taskDependencyService.getTaskRisk(
                        projectId,
                        taskId
                )
        );
    }
}