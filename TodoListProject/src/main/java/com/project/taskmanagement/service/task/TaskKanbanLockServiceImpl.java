package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.SprintRepository;
import com.project.taskmanagement.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskKanbanLockServiceImpl
        implements TaskKanbanLockService {

    TaskRepository taskRepository;
    SprintRepository sprintRepository;

    @Override
    public List<Task> lockColumn(
            UUID projectId,
            UUID sprintId,
            TaskStatus status
    ) {
        if (sprintId == null) {
            return taskRepository.lockBacklogColumnTasks(
                    projectId,
                    status
            );
        }

        // Khóa Sprint cha để cả cột rỗng cũng được tuần tự hóa.
        sprintRepository
                .findWithLockByIdAndProjectId(sprintId, projectId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SPRINT_NOT_FOUND
                ));

        return taskRepository.lockSprintColumnTasks(
                projectId,
                sprintId,
                status
        );
    }
}
