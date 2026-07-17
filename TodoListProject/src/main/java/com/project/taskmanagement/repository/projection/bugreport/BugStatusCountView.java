package com.project.taskmanagement.repository.projection.bugreport;

import com.project.taskmanagement.enums.BugStatus;

public interface BugStatusCountView {

    BugStatus getStatus();

    Long getTotal();
}
