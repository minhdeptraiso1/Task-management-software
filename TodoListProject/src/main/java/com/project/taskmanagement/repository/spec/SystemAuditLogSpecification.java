package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.dto.request.admin.SystemAuditSearchRequest;
import com.project.taskmanagement.entity.SystemAuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;

public final class SystemAuditLogSpecification {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private SystemAuditLogSpecification() {
    }

    public static Specification<SystemAuditLog> byRequest(
            SystemAuditSearchRequest request
    ) {
        return (root, query, cb) -> {
            if (request == null) {
                return cb.conjunction();
            }

            var predicates =
                    new ArrayList<Predicate>();

            if (request.actorUserId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("actorUserId"),
                                request.actorUserId()
                        )
                );
            }

            if (request.action() != null) {
                predicates.add(
                        cb.equal(
                                root.get("action"),
                                request.action()
                        )
                );
            }

            if (request.resourceType() != null) {
                predicates.add(
                        cb.equal(
                                root.get("resourceType"),
                                request.resourceType()
                        )
                );
            }

            if (request.resourceId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("resourceId"),
                                request.resourceId()
                        )
                );
            }

            if (request.success() != null) {
                predicates.add(
                        cb.equal(
                                root.get("success"),
                                request.success()
                        )
                );
            }

            if (request.fromDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                request.fromDate()
                                        .atStartOfDay(BUSINESS_ZONE)
                                        .toInstant()
                        )
                );
            }

            if (request.toDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                request.toDate()
                                        .atTime(LocalTime.MAX)
                                        .atZone(BUSINESS_ZONE)
                                        .toInstant()
                        )
                );
            }

            if (request.keyword() != null
                    && !request.keyword().isBlank()) {

                String keyword =
                        "%"
                                + request.keyword()
                                .trim()
                                .toLowerCase()
                                + "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("ipAddress")),
                                        keyword
                                ),
                                cb.like(
                                        cb.lower(root.get("userAgent")),
                                        keyword
                                ),
                                cb.like(
                                        cb.lower(root.get("errorMessage")),
                                        keyword
                                ),
                                cb.like(
                                        cb.lower(root.get("oldValueJson")),
                                        keyword
                                ),
                                cb.like(
                                        cb.lower(root.get("newValueJson")),
                                        keyword
                                )
                        )
                );
            }

            return cb.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}
