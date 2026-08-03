package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskColumnReorderService {

    void reorderColumn(
            List<Task> columnTasks,
            UUID movingTaskId,
            Long targetPosition
    );

    void normalizeColumn(List<Task> columnTasks);
}
