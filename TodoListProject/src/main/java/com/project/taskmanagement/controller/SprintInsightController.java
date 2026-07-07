package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.sprint.SprintCapacityResponse;
import com.project.taskmanagement.dto.response.sprint.SprintHealthResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRiskResponse;
import com.project.taskmanagement.service.SprintInsightService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
public class SprintInsightController {

    SprintInsightService sprintInsightService;

    // ===================== CAPACITY =====================

    @Operation(
            summary = "Lấy capacity của Sprint"
    )
    @GetMapping("/capacity")
    public ApiResponseSever<SprintCapacityResponse>
    getCapacity(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintInsightService
                        .getCapacity(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== HEALTH =====================

    @Operation(
            summary = "Lấy tình trạng sức khỏe Sprint"
    )
    @GetMapping("/health")
    public ApiResponseSever<SprintHealthResponse>
    getHealth(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintInsightService
                        .getHealth(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== RISKS =====================

    @Operation(
            summary = "Lấy danh sách rủi ro của Sprint"
    )
    @GetMapping("/risks")
    public ApiResponseSever<List<SprintRiskResponse>>
    getRisks(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintInsightService
                        .getRisks(
                                projectId,
                                sprintId
                        )
        );
    }
}
