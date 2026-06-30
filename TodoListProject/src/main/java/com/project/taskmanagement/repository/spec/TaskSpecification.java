package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Task;
import com.project.taskmanagement.enums.TaskPriority;
import com.project.taskmanagement.enums.TaskStatus;
import com.project.taskmanagement.enums.TaskType;
import org.springframework.data.jpa.domain.Specification;

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
}