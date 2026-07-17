package com.project.taskmanagement.repository.projection.bugreport;

import com.project.taskmanagement.enums.TaskPriority;

public interface BugPriorityCountView {

    TaskPriority getPriority();

    Long getTotal();
}
