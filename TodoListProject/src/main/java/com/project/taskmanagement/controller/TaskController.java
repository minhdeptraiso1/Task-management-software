package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.task.*;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.kanban.KanbanBoardResponse;
import com.project.taskmanagement.dto.response.task.TaskPageResponse;
import com.project.taskmanagement.dto.response.task.TaskResponse;
import com.project.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/projects/{projectId}/tasks"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskController {

    TaskService taskService;

    @Operation(
            summary = "Tạo Task"
    )
    @PostMapping
    public ResponseEntity<
            ApiResponseSever<TaskResponse>
            > create(
            @PathVariable
            UUID projectId,

            @Valid
            @RequestBody
            CreateTaskRequest request
    ) {
        TaskResponse response =
                taskService.create(
                        projectId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseSever.ok(response)
                );
    }

    @Operation(
            summary = "Tìm kiếm Task"
    )
    @GetMapping
    public ApiResponseSever<TaskPageResponse>
    search(
            @PathVariable
            UUID projectId,

            @Valid
            @ParameterObject
            TaskSearchRequest request,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                taskService.search(
                        projectId,
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Xem chi tiết Task"
    )
    @GetMapping("/{taskId}")
    public ApiResponseSever<TaskResponse>
    getById(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        return ApiResponseSever.ok(
                taskService.getById(
                        projectId,
                        taskId
                )
        );
    }

    @Operation(
            summary = "Cập nhật nội dung Task"
    )
    @PatchMapping("/{taskId}")
    public ApiResponseSever<TaskResponse>
    update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            UpdateTaskRequest request
    ) {
        return ApiResponseSever.ok(
                taskService.update(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Phân công Task"
    )
    @PatchMapping("/{taskId}/assign")
    public ApiResponseSever<TaskResponse>
    assign(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            AssignTaskRequest request
    ) {
        return ApiResponseSever.ok(
                taskService.assign(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Bỏ phân công Task"
    )
    @PatchMapping("/{taskId}/unassign")
    public ApiResponseSever<TaskResponse>
    unassign(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        return ApiResponseSever.ok(
                taskService.unassign(
                        projectId,
                        taskId
                )
        );
    }

    @Operation(
            summary = "Xóa mềm Task"
    )
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        taskService.delete(
                projectId,
                taskId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Lấy bảng Kanban của Sprint"
    )
    @GetMapping(
            "/../sprints/{sprintId}/kanban"
    )
    public ApiResponseSever<KanbanBoardResponse>
    getKanbanBoard(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                taskService.getKanbanBoard(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Chuyển Task sang cột Kanban khác"
    )
    @PatchMapping("/{taskId}/status")
    public ApiResponseSever<TaskResponse>
    updateStatus(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            UpdateTaskStatusRequest request
    ) {
        return ApiResponseSever.ok(
                taskService.updateStatus(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Thay đổi vị trí Task trong cột Kanban"
    )
    @PatchMapping("/{taskId}/position")
    public ApiResponseSever<TaskResponse>
    updatePosition(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            UpdateTaskPositionRequest request
    ) {
        return ApiResponseSever.ok(
                taskService.updatePosition(
                        projectId,
                        taskId,
                        request
                )
        );
    }
}
