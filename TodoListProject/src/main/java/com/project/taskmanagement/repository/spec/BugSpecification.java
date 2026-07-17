package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class BugSpecification {

    private BugSpecification() {
    }

    public static Specification<Bug> belongsToProject(
            UUID projectId
    ) {
        return (root, query, cb) ->
                cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Bug> search(
            String keyword
    ) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%"
                    + keyword.trim().toLowerCase()
                    + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Bug> hasStatus(BugStatus status) {
        return (root, query, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Bug> hasSeverity(BugSeverity severity) {
        return (root, query, cb) ->
                severity == null
                        ? cb.conjunction()
                        : cb.equal(root.get("severity"), severity);
    }

    public static Specification<Bug> hasPriority(TaskPriority priority) {
        return (root, query, cb) ->
                priority == null
                        ? cb.conjunction()
                        : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Bug> hasAssignee(UUID assigneeUserId) {
        return (root, query, cb) ->
                assigneeUserId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("assigneeUserId"), assigneeUserId);
    }

    public static Specification<Bug> hasReporter(UUID reporterUserId) {
        return (root, query, cb) ->
                reporterUserId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("reporterUserId"), reporterUserId);
    }

    public static Specification<Bug> hasTask(UUID taskId) {
        return (root, query, cb) ->
                taskId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("taskId"), taskId);
    }

    public static Specification<Bug> hasBacklogItem(UUID backlogItemId) {
        return (root, query, cb) ->
                backlogItemId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("backlogItemId"), backlogItemId);
    }

    public static Specification<Bug> hasSprint(UUID sprintId) {
        return (root, query, cb) ->
                sprintId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("sprintId"), sprintId);
    }
}
