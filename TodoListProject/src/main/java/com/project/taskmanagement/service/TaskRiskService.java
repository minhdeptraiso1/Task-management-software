package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.taskrisk.TaskRiskResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskScanResponse;
import com.project.taskmanagement.dto.response.taskrisk.TaskRiskSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface TaskRiskService {

    List<TaskRiskResponse> getProjectTaskRisks(
            UUID projectId
    );

    TaskRiskSummaryResponse getProjectRiskSummary(
            UUID projectId
    );

    TaskRiskSummaryResponse getSprintRiskSummary(
            UUID projectId,
            UUID sprintId
    );

    TaskRiskScanResponse scanProjectRisks(
            UUID projectId
    );
}
