package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.tasktimelog.CreateTaskTimeLogRequest;
import com.project.taskmanagement.dto.request.tasktimelog.UpdateTaskTimeLogRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogPageResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeSummaryResponse;
import com.project.taskmanagement.service.TaskTimeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = OpenApiTags.TIME_LOGS, description = "Ghi nhận và tổng hợp thời gian làm việc trên Task")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(
        "/projects/{projectId}/tasks/{taskId}"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskTimeLogController {

    TaskTimeLogService taskTimeLogService;

    @Operation(
            summary = "Ghi thời gian làm việc cho Task"
    )
    @PostMapping("/time-logs")
    public ApiResponseSever<TaskTimeLogResponse>
    create(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @Valid
            @RequestBody
            CreateTaskTimeLogRequest request
    ) {
        return ApiResponseSever.ok(
                taskTimeLogService.create(
                        projectId,
                        taskId,
                        request
                )
        );
    }

    @Operation(
            summary = "Lấy danh sách time log của Task"
    )
    @GetMapping("/time-logs")
    public ApiResponseSever<TaskTimeLogPageResponse>
    getTimeLogs(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                taskTimeLogService.getTimeLogs(
                        projectId,
                        taskId,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Cập nhật time log"
    )
    @RequestMapping(
            value = "/time-logs/{timeLogId}",
            method = {
                    RequestMethod.PUT,
                    RequestMethod.PATCH
            }
    )
    public ApiResponseSever<TaskTimeLogResponse>
    update(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @PathVariable
            UUID timeLogId,

            @Valid
            @RequestBody
            UpdateTaskTimeLogRequest request
    ) {
        return ApiResponseSever.ok(
                taskTimeLogService.update(
                        projectId,
                        taskId,
                        timeLogId,
                        request
                )
        );
    }

    @Operation(
            summary = "Xóa mềm time log"
    )
    @DeleteMapping(
            "/time-logs/{timeLogId}"
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId,

            @PathVariable
            UUID timeLogId
    ) {
        taskTimeLogService.delete(
                projectId,
                taskId,
                timeLogId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(
            summary = "Lấy tổng hợp thời gian Task"
    )
    @GetMapping("/time-summary")
    public ApiResponseSever<TaskTimeSummaryResponse>
    getSummary(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID taskId
    ) {
        return ApiResponseSever.ok(
                taskTimeLogService.getSummary(
                        projectId,
                        taskId
                )
        );
    }
}
