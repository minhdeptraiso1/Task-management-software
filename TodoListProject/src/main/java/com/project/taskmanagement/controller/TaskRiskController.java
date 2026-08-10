package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskScanResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskSummaryResponse;
import com.project.taskmanagement.service.TaskRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = OpenApiTags.TASKS, description = "Rủi ro, blocker và cảnh báo Task")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskRiskController {

    TaskRiskService taskRiskService;

    @Operation(
            summary = "Get Project Task risks"
    )
    @GetMapping("/task-risks")
    public ApiResponseSever<List<TaskRiskResponse>>
    getProjectTaskRisks(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                taskRiskService.getProjectTaskRisks(projectId)
        );
    }

    @Operation(
            summary = "Get Project Task risk summary"
    )
    @GetMapping("/task-risks/summary")
    public ApiResponseSever<TaskRiskSummaryResponse>
    getProjectRiskSummary(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                taskRiskService.getProjectRiskSummary(projectId)
        );
    }

    @Operation(
            summary = "Get Sprint Task risk summary"
    )
    @GetMapping("/sprints/{sprintId}/task-risks/summary")
    public ApiResponseSever<TaskRiskSummaryResponse>
    getSprintRiskSummary(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                taskRiskService.getSprintRiskSummary(
                        projectId,
                        sprintId
                )
        );
    }

    @Operation(
            summary = "Scan Project Task risks"
    )
    @PostMapping("/task-risks/scan")
    public ApiResponseSever<TaskRiskScanResponse>
    scanProjectRisks(
            @PathVariable
            UUID projectId
    ) {
        return ApiResponseSever.ok(
                taskRiskService.scanProjectRisks(projectId)
        );
    }
}
