package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.service.model.TaskImportRowData;

import java.util.List;
import java.util.UUID;

public interface TaskImportWriterService {

    List<UUID> importAll(
            UUID projectId,
            UUID sprintId,
            UUID actorUserId,
            List<TaskImportRowData> rows,
            TaskImportBatch batch
    );
}