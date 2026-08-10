package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.report.ProjectMemberReportRequest;
import com.project.taskmanagement.dto.request.report.ProjectTimeReportRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.report.ProjectMemberReportResponse;
import com.project.taskmanagement.dto.response.report.ProjectTimeReportResponse;
import com.project.taskmanagement.dto.response.report.SprintReportResponse;
import com.project.taskmanagement.service.ProjectReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.REPORTS, description = "Báo cáo Sprint, thành viên và thời gian làm việc của Project")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/reports")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectReportController {

    ProjectReportService projectReportService;

    // ===================== SPRINT REPORT =====================

    @Operation(
            summary = "Lấy báo cáo Sprint"
    )
    @GetMapping("/sprints/{sprintId}")
    public ApiResponseSever<SprintReportResponse>
    getSprintReport(
            @PathVariable
            UUID projectId,

            @PathVariable
            UUID sprintId
    ) {
        return ApiResponseSever.ok(
                projectReportService
                        .getSprintReport(
                                projectId,
                                sprintId
                        )
        );
    }

    // ===================== MEMBER REPORT =====================

    @Operation(
            summary = "Lấy báo cáo hiệu suất thành viên"
    )
    @GetMapping("/members")
    public ApiResponseSever<ProjectMemberReportResponse>
    getMemberReport(
            @PathVariable
            UUID projectId,

            @ParameterObject
            ProjectMemberReportRequest request
    ) {
        return ApiResponseSever.ok(
                projectReportService
                        .getMemberReport(
                                projectId,
                                request
                        )
        );
    }

    // ===================== TIME REPORT =====================

    @Operation(
            summary = "Lấy báo cáo thời gian Project"
    )
    @GetMapping("/time")
    public ApiResponseSever<ProjectTimeReportResponse>
    getTimeReport(
            @PathVariable
            UUID projectId,

            @ParameterObject
            ProjectTimeReportRequest request
    ) {
        return ApiResponseSever.ok(
                projectReportService
                        .getTimeReport(
                                projectId,
                                request
                        )
        );
    }
}
