package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.enums.BacklogItemStatus;
import com.project.taskmanagement.enums.BacklogItemType;
import com.project.taskmanagement.enums.BacklogPriority;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.UUID;

public final class BacklogItemSpecification {

    private BacklogItemSpecification() {
    }

    public static Specification<BacklogItem> belongsToProject(
            UUID projectId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("projectId"),
                        projectId
                );
    }

    public static Specification<BacklogItem> search(
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String normalizedKeyword =
                    "%"
                            + keyword
                            .trim()
                            .toLowerCase(Locale.ROOT)
                            + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("title")
                            ),
                            normalizedKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("description")
                            ),
                            normalizedKeyword
                    )
            );
        };
    }

    public static Specification<BacklogItem> hasStatus(
            BacklogItemStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<BacklogItem> hasType(
            BacklogItemType type
    ) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("type"),
                    type
            );
        };
    }

    public static Specification<BacklogItem> hasPriority(
            BacklogPriority priority
    ) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("priority"),
                    priority
            );
        };
    }

    public static Specification<BacklogItem> belongsToSprint(
            UUID sprintId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (sprintId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("sprintId"),
                    sprintId
            );
        };
    }

    public static Specification<BacklogItem> unscheduledOnly(
            Boolean unscheduledOnly
    ) {
        return (root, query, criteriaBuilder) -> {
            if (!Boolean.TRUE.equals(unscheduledOnly)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.isNull(
                    root.get("sprintId")
            );
        };
    }
}