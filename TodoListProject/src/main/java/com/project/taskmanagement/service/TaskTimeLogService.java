package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.tasktimelog.CreateTaskTimeLogRequest;
import com.project.taskmanagement.dto.request.tasktimelog.UpdateTaskTimeLogRequest;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogPageResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeLogResponse;
import com.project.taskmanagement.dto.response.tasktimelog.TaskTimeSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskTimeLogService {

    TaskTimeLogResponse create(
            UUID projectId,
            UUID taskId,
            CreateTaskTimeLogRequest request
    );

    TaskTimeLogPageResponse getTimeLogs(
            UUID projectId,
            UUID taskId,
            Pageable pageable
    );

    TaskTimeLogResponse update(
            UUID projectId,
            UUID taskId,
            UUID timeLogId,
            UpdateTaskTimeLogRequest request
    );

    void delete(
            UUID projectId,
            UUID taskId,
            UUID timeLogId
    );

    TaskTimeSummaryResponse getSummary(
            UUID projectId,
            UUID taskId
    );
}