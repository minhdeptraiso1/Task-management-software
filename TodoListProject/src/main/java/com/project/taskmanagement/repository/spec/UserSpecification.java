
package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class UserSpecification {

    private UserSpecification() {
    }

    /**
     * Tìm kiếm theo username hoặc email.
     */
    public static Specification<User> search(
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
                                    root.get("username")
                            ),
                            normalizedKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("email")
                            ),
                            normalizedKeyword
                    )
            );
        };
    }

    /**
     * Lọc theo vai trò người dùng.
     */
    public static Specification<User> hasRole(
            UserRole role
    ) {
        return (root, query, criteriaBuilder) -> {

            if (role == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("role"),
                    role
            );
        };
    }

    /**
     * Lọc theo trạng thái hoạt động.
     */
    public static Specification<User> isEnabled(
            Boolean enabled
    ) {
        return (root, query, criteriaBuilder) -> {

            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("enabled"),
                    enabled
            );
        };
    }

    public static Specification<User> roleIsNot(
            UserRole role
    ) {
        return (root, query, criteriaBuilder) -> {

            if (role == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.notEqual(
                    root.get("role"),
                    role
            );
        };
    }
}

