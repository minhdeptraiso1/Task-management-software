package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TaskViewHelper {

    public boolean isOverdue(
            Task task
    ) {
        return task.getDueDate() != null
                && task.getDueDate()
                .isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE
                && task.getStatus() != TaskStatus.CANCELLED;
    }

    public boolean isBlocked(
            Task task
    ) {
        return task.getStatus() == TaskStatus.BLOCKED;
    }

    public String targetUrl(
            Task task
    ) {
        return "/projects/"
                + task.getProjectId()
                + "/tasks/"
                + task.getId();
    }
}
