package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.OpenApiTags;
import com.project.taskmanagement.dto.request.timesheet.TimesheetSearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.timesheet.TimesheetPageResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetSummaryResponse;
import com.project.taskmanagement.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = OpenApiTags.TIME_LOGS, description = "Timesheet cá nhân và Project theo khoảng thời gian")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class TimesheetController {

    TimesheetService timesheetService;

    @Operation(
            summary = "Lay timesheet ca nhan"
    )
    @GetMapping("/timesheets/me")
    public ApiResponseSever<TimesheetPageResponse> getMyTimesheet(
            @ParameterObject
            TimesheetSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                timesheetService.getMyTimesheet(
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Lay tong hop timesheet ca nhan"
    )
    @GetMapping("/timesheets/me/summary")
    public ApiResponseSever<TimesheetSummaryResponse> getMyTimesheetSummary(
            @ParameterObject
            TimesheetSearchRequest request
    ) {
        return ApiResponseSever.ok(
                timesheetService
                        .getMyTimesheetSummary(request)
        );
    }

    @Operation(
            summary = "Lay timesheet project"
    )
    @GetMapping("/projects/{projectId}/timesheets")
    public ApiResponseSever<TimesheetPageResponse> getProjectTimesheet(
            @PathVariable
            UUID projectId,

            @ParameterObject
            TimesheetSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                timesheetService.getProjectTimesheet(
                        projectId,
                        request,
                        pageable
                )
        );
    }

    @Operation(
            summary = "Lay tong hop timesheet project"
    )
    @GetMapping("/projects/{projectId}/timesheets/summary")
    public ApiResponseSever<TimesheetSummaryResponse> getProjectTimesheetSummary(
            @PathVariable
            UUID projectId,

            @ParameterObject
            TimesheetSearchRequest request
    ) {
        return ApiResponseSever.ok(
                timesheetService.getProjectTimesheetSummary(
                        projectId,
                        request
                )
        );
    }
}
