package com.project.taskmanagement.repository.spec;

import com.project.taskmanagement.entity.TaskImportBatch;
import com.project.taskmanagement.enums.TaskImportStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public final class TaskImportBatchSpecification {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private TaskImportBatchSpecification() {
    }

    public static Specification<TaskImportBatch> belongsToProject(
            UUID projectId
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("projectId"),
                        projectId
                );
    }

    public static Specification<TaskImportBatch> hasStatus(
            TaskImportStatus status
    ) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<TaskImportBatch> hasSprintId(
            UUID sprintId
    ) {
        return (root, query, cb) -> sprintId == null
                ? cb.conjunction()
                : cb.equal(root.get("sprintId"), sprintId);
    }

    public static Specification<TaskImportBatch> importedBy(
            UUID importedByUserId
    ) {
        return (root, query, cb) -> importedByUserId == null
                ? cb.conjunction()
                : cb.equal(root.get("importedByUserId"), importedByUserId);
    }

    public static Specification<TaskImportBatch> createdFrom(
            LocalDate fromDate
    ) {
        return (root, query, cb) -> fromDate == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(
                root.get("createdAt"),
                fromDate.atStartOfDay(BUSINESS_ZONE).toInstant()
        );
    }

    public static Specification<TaskImportBatch> createdTo(
            LocalDate toDate
    ) {
        return (root, query, cb) -> toDate == null
                ? cb.conjunction()
                : cb.lessThan(
                root.get("createdAt"),
                toDate.plusDays(1)
                        .atStartOfDay(BUSINESS_ZONE)
                        .toInstant()
        );
    }
}
