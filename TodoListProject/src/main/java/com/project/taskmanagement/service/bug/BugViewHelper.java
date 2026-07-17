package com.project.taskmanagement.service.bug;

import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.enums.BugStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class BugViewHelper {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public boolean isOverdue(Bug bug) {
        return bug.getDueDate() != null
                && bug.getDueDate().isBefore(LocalDate.now(BUSINESS_ZONE))
                && bug.getStatus() != BugStatus.RESOLVED
                && bug.getStatus() != BugStatus.VERIFIED
                && bug.getStatus() != BugStatus.CLOSED
                && bug.getStatus() != BugStatus.CANCELLED;
    }

    public String targetUrl(Bug bug) {
        return "/projects/" + bug.getProjectId() + "/bugs/" + bug.getId();
    }
}
