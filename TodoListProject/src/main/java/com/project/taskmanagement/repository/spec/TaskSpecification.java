package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> belongsToProject(
            UUID projectId
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("projectId"),
                        projectId
                );
    }

    public static Specification<Task> search(
            String keyword
    ) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String normalized =
                    "%"
                            + keyword.trim()
                            .toLowerCase(Locale.ROOT)
                            + "%";

            return cb.or(
                    cb.like(
                            cb.lower(
                                    root.get("title")
                            ),
                            normalized
                    ),
                    cb.like(
                            cb.lower(
                                    root.get("description")
                            ),
                            normalized
                    )
            );
        };
    }

    public static Specification<Task> hasBacklogItemId(
            UUID backlogItemId
    ) {
        return (root, query, cb) -> {
            if (backlogItemId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("backlogItemId"),
                    backlogItemId
            );
        };
    }

    public static Specification<Task> hasSprintId(
            UUID sprintId
    ) {
        return (root, query, cb) -> {
            if (sprintId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("currentSprintId"),
                    sprintId
            );
        };
    }

    public static Specification<Task> hasAssignee(
            UUID assigneeUserId
    ) {
        return (root, query, cb) -> {
            if (assigneeUserId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("assigneeUserId"),
                    assigneeUserId
            );
        };
    }

    public static Specification<Task> hasReporter(
            UUID reporterUserId
    ) {
        return (root, query, cb) -> {
            if (reporterUserId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("reporterUserId"),
                    reporterUserId
            );
        };
    }

    public static Specification<Task> hasStatus(
            TaskStatus status
    ) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Task> hasPriority(
            TaskPriority priority
    ) {
        return (root, query, cb) -> {
            if (priority == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("priority"),
                    priority
            );
        };
    }

    public static Specification<Task> hasType(
            TaskType type
    ) {
        return (root, query, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("type"),
                    type
            );
        };
    }

    public static Specification<Task> unassignedOnly(
            Boolean unassignedOnly
    ) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(unassignedOnly)) {
                return cb.conjunction();
            }

            return cb.isNull(
                    root.get("assigneeUserId")
            );
        };
    }

    public static Specification<Task> statusNotIn(
            Collection<TaskStatus> statuses
    ) {
        return (root, query, cb) -> {

            if (statuses == null
                    || statuses.isEmpty()) {

                return cb.conjunction();
            }

            return cb.not(
                    root.get("status")
                            .in(statuses)
            );
        };
    }

    public static Specification<Task> dueDateBefore(
            LocalDate date
    ) {
        return (root, query, cb) -> {

            if (date == null) {
                return cb.conjunction();
            }

            return cb.lessThan(
                    root.get("dueDate"),
                    date
            );
        };
    }

    public static Specification<Task> dueDateBetween(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {

            if (fromDate == null && toDate == null) {
                return cb.conjunction();
            }

            if (fromDate != null && toDate != null) {
                return cb.between(
                        root.get("dueDate"),
                        fromDate,
                        toDate
                );
            }

            if (fromDate != null) {
                return cb.greaterThanOrEqualTo(
                        root.get("dueDate"),
                        fromDate
                );
            }

            return cb.lessThanOrEqualTo(
                    root.get("dueDate"),
                    toDate
            );
        };
    }

    public static Specification<Task> startDateBetween(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return localDateBetween("startDate", fromDate, toDate);
    }

    public static Specification<Task> createdAtBetween(
            Instant from,
            Instant to
    ) {
        return instantBetween("createdAt", from, to);
    }

    public static Specification<Task> overdueOnly(
            Boolean overdueOnly,
            LocalDate today
    ) {
        return (root, query, cb) -> {

            if (!Boolean.TRUE.equals(overdueOnly)) {
                return cb.conjunction();
            }

            return cb.and(
                    cb.isNotNull(
                            root.get("dueDate")
                    ),
                    cb.lessThan(
                            root.get("dueDate"),
                            today
                    ),
                    cb.not(
                            root.get("status")
                                    .in(
                                            TaskStatus.DONE,
                                            TaskStatus.CANCELLED
                                    )
                    )
            );
        };
    }

    public static Specification<Task> dueSoonOnly(
            Boolean dueSoonOnly,
            LocalDate today,
            LocalDate dueSoonEndDate
    ) {
        return (root, query, cb) -> {

            if (!Boolean.TRUE.equals(dueSoonOnly)) {
                return cb.conjunction();
            }

            return cb.and(
                    cb.isNotNull(
                            root.get("dueDate")
                    ),
                    cb.between(
                            root.get("dueDate"),
                            today,
                            dueSoonEndDate
                    ),
                    cb.not(
                            root.get("status")
                                    .in(
                                            TaskStatus.DONE,
                                            TaskStatus.CANCELLED
                                    )
                    )
            );
        };
    }

    private static Specification<Task> localDateBetween(
            String fieldName,
            LocalDate from,
            LocalDate to
    ) {
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

    private static Specification<Task> instantBetween(
            String fieldName,
            Instant from,
            Instant to
    ) {
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
