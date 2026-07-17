package com.project.taskmanagement.repository.projection.bugreport;

import com.project.taskmanagement.enums.BugSeverity;

public interface BugSeverityCountView {

    BugSeverity getSeverity();

    Long getTotal();
}
