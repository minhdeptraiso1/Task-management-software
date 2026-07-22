package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Bug;
import com.project.taskmanagement.enums.BugSeverity;
import com.project.taskmanagement.enums.BugStatus;
import com.project.taskmanagement.enums.TaskPriority;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
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
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("reproductionSteps")), pattern),
                    cb.like(cb.lower(root.get("expectedResult")), pattern),
                    cb.like(cb.lower(root.get("actualResult")), pattern)
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

    public static Specification<Bug> hasLinkedTask(UUID linkedTaskId) {
        return hasTask(linkedTaskId);
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

    public static Specification<Bug> reopenedOnly(Boolean reopenedOnly) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(reopenedOnly)) {
                return cb.conjunction();
            }

            return cb.greaterThan(root.get("reopenedCount"), 0);
        };
    }

    public static Specification<Bug> overdueOnly(Boolean overdueOnly, LocalDate today) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(overdueOnly)) {
                return cb.conjunction();
            }

            return cb.and(
                    cb.isNotNull(root.get("dueDate")),
                    cb.lessThan(root.get("dueDate"), today),
                    cb.not(root.get("status").in(
                            BugStatus.RESOLVED,
                            BugStatus.CLOSED,
                            BugStatus.CANCELLED
                    ))
            );
        };
    }

    public static Specification<Bug> dueDateBetween(LocalDate from, LocalDate to) {
        return localDateBetween("dueDate", from, to);
    }

    public static Specification<Bug> createdAtBetween(Instant from, Instant to) {
        return instantBetween("createdAt", from, to);
    }

    private static Specification<Bug> localDateBetween(String fieldName, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get(fieldName), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get(fieldName), from);
            }
            return cb.lessThanOrEqualTo(root.get(fieldName), to);
        };
    }

    private static Specification<Bug> instantBetween(String fieldName, Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get(fieldName), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get(fieldName), from);
            }
            return cb.lessThanOrEqualTo(root.get(fieldName), to);
        };
    }
}
