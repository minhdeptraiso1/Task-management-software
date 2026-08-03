package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskKanbanLockService {

    List<Task> lockColumn(
            UUID projectId,
            UUID sprintId,
            TaskStatus status
    );
}
