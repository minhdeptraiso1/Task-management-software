package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.taskstatistics.SprintBurndownResponse;
import com.project.taskmanagement.dto.response.taskstatistics.SprintTaskStatisticsResponse;
import com.project.taskmanagement.service.TaskStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(
        "/projects/{projectId}/sprints/{sprintId}"
)
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TaskStatisticsController {

    TaskStatisticsService taskStatisticsService;

    @Operation(
            summary = "Lấy thống kê Task của Sprint",
            description = """
                    Trả dữ liệu tổng hợp để frontend vẽ:
                    - biểu đồ Task theo trạng thái
                    - workload theo assignee
                    - estimated so với spent time
                    - tiến độ theo Backlog Item
                    """
    )
    @GetMapping("/task-statistics")
    public ApiResponseSever<
            SprintTaskStatisticsResponse
            > getTaskStatistics(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                taskStatisticsService
                        .getSprintStatistics(
                                projectId,
                                sprintId
                        )
        );
    }

    @Operation(
            summary = "Lấy dữ liệu biểu đồ Burndown"
    )
    @GetMapping("/burndown")
    public ApiResponseSever<SprintBurndownResponse>
    getBurndown(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                taskStatisticsService
                        .getSprintBurndown(
                                projectId,
                                sprintId
                        )
        );
    }
}