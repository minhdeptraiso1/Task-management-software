package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.sprint.UpdateSprintRetrospectiveRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintReviewRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.sprint.SprintClosingReportResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRetrospectiveResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReviewResponse;
import com.project.taskmanagement.service.SprintClosingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/projects/{projectId}/sprints/{sprintId}")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SprintClosingController {

    SprintClosingService sprintClosingService;

    @Operation(summary = "Lấy báo cáo đóng Sprint")
    @GetMapping("/closing-report")
    public ApiResponseSever<SprintClosingReportResponse>
    getClosingReport(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintClosingService
                        .getClosingReport(
                                projectId,
                                sprintId
                        )
        );
    }

    @Operation(summary = "Lấy Sprint Review")
    @GetMapping("/review")
    public ApiResponseSever<SprintReviewResponse>
    getReview(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintClosingService
                        .getReview(
                                projectId,
                                sprintId
                        )
        );
    }

    @Operation(summary = "Cập nhật Sprint Review")
    @PutMapping("/review")
    public ApiResponseSever<SprintReviewResponse>
    updateReview(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @RequestBody
            UpdateSprintReviewRequest request
    ) {
        return ApiResponseSever.ok(
                sprintClosingService
                        .updateReview(
                                projectId,
                                sprintId,
                                request
                        )
        );
    }

    @Operation(summary = "Lấy Sprint Retrospective")
    @GetMapping("/retrospective")
    public ApiResponseSever<SprintRetrospectiveResponse>
    getRetrospective(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                sprintClosingService
                        .getRetrospective(
                                projectId,
                                sprintId
                        )
        );
    }

    @Operation(summary = "Cập nhật Sprint Retrospective")
    @PutMapping("/retrospective")
    public ApiResponseSever<SprintRetrospectiveResponse>
    updateRetrospective(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId,

            @RequestBody
            UpdateSprintRetrospectiveRequest request
    ) {
        return ApiResponseSever.ok(
                sprintClosingService
                        .updateRetrospective(
                                projectId,
                                sprintId,
                                request
                        )
        );
    }
}
