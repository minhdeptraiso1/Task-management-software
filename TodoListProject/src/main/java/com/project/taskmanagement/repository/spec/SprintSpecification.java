package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.SprintStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.UUID;

public final class SprintSpecification {

    private SprintSpecification() {
    }

    public static Specification<Sprint> belongsToProject(
            UUID projectId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("projectId"),
                        projectId
                );
    }

    public static Specification<Sprint> search(
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
                                    root.get("name")
                            ),
                            normalizedKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("goal")
                            ),
                            normalizedKeyword
                    )
            );
        };
    }

    public static Specification<Sprint> hasStatus(
            SprintStatus status
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
}