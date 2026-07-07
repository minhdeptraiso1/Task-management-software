package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.sprint.UpdateSprintRetrospectiveRequest;
import com.project.taskmanagement.dto.request.sprint.UpdateSprintReviewRequest;
import com.project.taskmanagement.dto.response.sprint.SprintClosingReportResponse;
import com.project.taskmanagement.dto.response.sprint.SprintRetrospectiveResponse;
import com.project.taskmanagement.dto.response.sprint.SprintReviewResponse;

import java.util.UUID;

public interface SprintClosingService {

    SprintClosingReportResponse getClosingReport(
            UUID projectId,
            UUID sprintId
    );

    SprintReviewResponse getReview(
            UUID projectId,
            UUID sprintId
    );

    SprintReviewResponse updateReview(
            UUID projectId,
            UUID sprintId,
            UpdateSprintReviewRequest request
    );

    SprintRetrospectiveResponse getRetrospective(
            UUID projectId,
            UUID sprintId
    );

    SprintRetrospectiveResponse updateRetrospective(
            UUID projectId,
            UUID sprintId,
            UpdateSprintRetrospectiveRequest request
    );
}
