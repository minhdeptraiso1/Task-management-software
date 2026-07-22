package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Sprint;
import com.project.taskmanagement.enums.SprintStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
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

    public static Specification<Sprint> startDateBetween(LocalDate from, LocalDate to) {
        return localDateBetween("startDate", from, to);
    }

    public static Specification<Sprint> endDateBetween(LocalDate from, LocalDate to) {
        return localDateBetween("endDate", from, to);
    }

    public static Specification<Sprint> createdAtBetween(Instant from, Instant to) {
        return instantBetween("createdAt", from, to);
    }

    private static Specification<Sprint> localDateBetween(String fieldName, LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get(fieldName), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), to);
        };
    }

    private static Specification<Sprint> instantBetween(String fieldName, Instant from, Instant to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get(fieldName), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), to);
        };
    }
}
