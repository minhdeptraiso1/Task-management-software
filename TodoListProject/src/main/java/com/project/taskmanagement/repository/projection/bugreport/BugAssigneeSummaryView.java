package com.project.taskmanagement.repository.projection.bugreport;

import java.util.UUID;

public interface BugAssigneeSummaryView {

    UUID getAssigneeUserId();

    String getUsername();

    String getEmail();

    Long getTotalBugs();

    Long getOpenBugs();

    Long getResolvedBugs();

    Long getClosedBugs();

    Long getOverdueBugs();

    Long getCriticalBugs();

    Long getReopenedBugs();
}
