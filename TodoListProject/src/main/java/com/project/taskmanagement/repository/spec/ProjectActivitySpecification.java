package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.enums.ActivityEntityType;
import com.project.taskmanagement.enums.ProjectActivityAction;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class ProjectActivitySpecification {

    private ProjectActivitySpecification() {
    }

    public static Specification<ProjectActivityLog>
    hasProjectId(
            UUID projectId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("projectId"),
                        projectId
                );
    }

    public static Specification<ProjectActivityLog>
    hasEntityType(
            ActivityEntityType entityType
    ) {
        return (root, query, criteriaBuilder) -> {

            if (entityType == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("entityType"),
                    entityType
            );
        };
    }

    public static Specification<ProjectActivityLog>
    hasEntityId(
            UUID entityId
    ) {
        return (root, query, criteriaBuilder) -> {

            if (entityId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("entityId"),
                    entityId
            );
        };
    }

    public static Specification<ProjectActivityLog>
    hasAction(
            ProjectActivityAction action
    ) {
        return (root, query, criteriaBuilder) -> {

            if (action == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("action"),
                    action
            );
        };
    }

    public static Specification<ProjectActivityLog>
    hasPerformedByUserId(
            UUID performedByUserId
    ) {
        return (root, query, criteriaBuilder) -> {

            if (performedByUserId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("performedByUserId"),
                    performedByUserId
            );
        };
    }

    public static Specification<ProjectActivityLog>
    createdFrom(
            Instant fromDate
    ) {
        return (root, query, criteriaBuilder) -> {

            if (fromDate == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    fromDate
            );
        };
    }

    public static Specification<ProjectActivityLog>
    createdTo(
            Instant toDate
    ) {
        return (root, query, criteriaBuilder) -> {

            if (toDate == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    toDate
            );
        };
    }

    public static Specification<ProjectActivityLog>
    searchKeyword(
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {

            if (keyword == null
                    || keyword.isBlank()) {

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
                                    root.get("oldValueJson")
                            ),
                            normalizedKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("newValueJson")
                            ),
                            normalizedKeyword
                    )
            );
        };
    }
}