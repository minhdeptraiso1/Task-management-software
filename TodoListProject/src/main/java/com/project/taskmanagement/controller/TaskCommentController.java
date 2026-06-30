package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.taskcomment.CreateTaskCommentRequest;
import com.project.taskmanagement.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentPageResponse;
import com.project.taskmanagement.dto.response.taskcomment.TaskCommentResponse;
import com.project.taskmanagement.service.TaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/projects/{projectId}/tasks/{taskId}/comments"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskCommentController {

    TaskCommentService taskCommentService;

    @Operation(
            summary = "Tạo comment hoặc reply Task"
    )
    @PostMapping
    public ApiResponseSever<TaskCommentResponse>
    create(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            CreateTaskCommentRequest request
    ) {
        return ApiResponseSever.ok(
                taskCommentService.create(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách comment Task"
    )
    @GetMapping
    public ApiResponseSever<TaskCommentPageResponse>
    getComments(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                taskCommentService.getComments(
                        projectId,
                        taskId,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Chỉnh sửa comment Task"
    )
    @PatchMapping("/{commentId}")
    public ApiResponseSever<TaskCommentResponse>
    update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @PathVariable
            UUID commentId,

            @Valid
            @RequestBody
            UpdateTaskCommentRequest request
    ) {
        return ApiResponseSever.ok(
                taskCommentService.update(
                        projectId,
                        taskId,
                        commentId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa mềm comment Task"
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @PathVariable
            UUID commentId
    ) {
        taskCommentService.delete(
                projectId,
                taskId,
                commentId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}