package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.timesheet.TimesheetSearchRequest;
import com.project.taskmanagement.dto.response.timesheet.TimesheetPageResponse;
import com.project.taskmanagement.dto.response.timesheet.TimesheetSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TimesheetService {

    TimesheetPageResponse getMyTimesheet(
            TimesheetSearchRequest request,
            Pageable pageable
    );

    TimesheetSummaryResponse getMyTimesheetSummary(
            TimesheetSearchRequest request
    );

    TimesheetPageResponse getProjectTimesheet(
            UUID projectId,
            TimesheetSearchRequest request,
            Pageable pageable
    );

    TimesheetSummaryResponse getProjectTimesheetSummary(
            UUID projectId,
            TimesheetSearchRequest request
    );
}
