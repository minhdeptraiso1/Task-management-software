package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.enums.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> search(
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
                                    root.get("code")
                            ),
                            normalizedKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("name")
                            ),
                            normalizedKeyword
                    )
            );
        };
    }

    public static Specification<Project> hasStatus(
            ProjectStatus status
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

    public static Specification<Project> idIn(
            Collection<UUID> projectIds
    ) {
        return (root, query, criteriaBuilder) -> {

            if (projectIds == null) {
                return criteriaBuilder.conjunction();
            }

            if (projectIds.isEmpty()) {
                return criteriaBuilder.disjunction();
            }

            return root
                    .get("id")
                    .in(projectIds);
        };
    }
}