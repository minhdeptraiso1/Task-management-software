package com.project.taskmanagement.repository.impl;

import com.project.taskmanagement.enums.SearchEntityType;
import com.project.taskmanagement.repository.SearchRepository;
import com.project.taskmanagement.repository.projection.search.SearchResultView;
import com.project.taskmanagement.repository.projection.search.SearchResultViewImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SearchRepositoryImpl implements SearchRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<SearchResultView> search(
            String keyword,
            SearchEntityType entityType,
            UUID projectId,
            List<UUID> allowedProjectIds,
            boolean admin,
            int limit
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        if (!admin && (allowedProjectIds == null || allowedProjectIds.isEmpty())) {
            return List.of();
        }

        String likeKeyword = "%" + normalizedKeyword.toLowerCase() + "%";
        List<String> parts = new ArrayList<>();

        if (shouldInclude(entityType, SearchEntityType.PROJECT)) {
            parts.add(projectSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.SPRINT)) {
            parts.add(sprintSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.BACKLOG_ITEM)) {
            parts.add(backlogItemSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.TASK)) {
            parts.add(taskSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.BUG)) {
            parts.add(bugSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.COMMENT)) {
            parts.add(commentSearchSql());
        }
        if (shouldInclude(entityType, SearchEntityType.ATTACHMENT)) {
            parts.add(attachmentSearchSql());
        }

        if (parts.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT * FROM ("
                + String.join(" UNION ALL ", parts)
                + ") result "
                + "WHERE (CAST(:projectId AS uuid) IS NULL OR result.project_id = CAST(:projectId AS uuid)) ";

        if (!admin) {
            sql += "AND result.project_id IN (:allowedProjectIds) ";
        }

        sql += "ORDER BY result.updated_at DESC NULLS LAST LIMIT :limit";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("keyword", likeKeyword);
        query.setParameter("projectId", projectId);
        query.setParameter("limit", limit);

        if (!admin) {
            query.setParameter("allowedProjectIds", allowedProjectIds);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(this::toView)
                .map(SearchResultView.class::cast)
                .toList();
    }

    private String projectSearchSql() {
        return """
                SELECT
                    'PROJECT' AS entity_type,
                    p.id AS entity_id,
                    p.name AS title,
                    p.description AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(p.updated_at, p.created_at) AS updated_at
                FROM projects p
                WHERE p.deleted_at IS NULL
                  AND (
                        LOWER(p.name) LIKE :keyword
                        OR LOWER(p.code) LIKE :keyword
                        OR LOWER(COALESCE(p.description, '')) LIKE :keyword
                  )
                """;
    }

    private String sprintSearchSql() {
        return """
                SELECT
                    'SPRINT' AS entity_type,
                    s.id AS entity_id,
                    s.name AS title,
                    s.goal AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(s.updated_at, s.created_at) AS updated_at
                FROM sprints s
                JOIN projects p ON p.id = s.project_id
                WHERE s.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND (
                        LOWER(s.name) LIKE :keyword
                        OR LOWER(COALESCE(s.goal, '')) LIKE :keyword
                  )
                """;
    }

    private String backlogItemSearchSql() {
        return """
                SELECT
                    'BACKLOG_ITEM' AS entity_type,
                    bi.id AS entity_id,
                    bi.title AS title,
                    bi.description AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(bi.updated_at, bi.created_at) AS updated_at
                FROM backlog_items bi
                JOIN projects p ON p.id = bi.project_id
                WHERE bi.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND (
                        LOWER(bi.title) LIKE :keyword
                        OR LOWER(COALESCE(bi.description, '')) LIKE :keyword
                  )
                """;
    }

    private String taskSearchSql() {
        return """
                SELECT
                    'TASK' AS entity_type,
                    t.id AS entity_id,
                    t.title AS title,
                    t.description AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(t.updated_at, t.created_at) AS updated_at
                FROM tasks t
                JOIN projects p ON p.id = t.project_id
                WHERE t.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND (
                        LOWER(t.title) LIKE :keyword
                        OR LOWER(COALESCE(t.description, '')) LIKE :keyword
                  )
                """;
    }

    private String bugSearchSql() {
        return """
                SELECT
                    'BUG' AS entity_type,
                    b.id AS entity_id,
                    b.title AS title,
                    b.description AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(b.updated_at, b.created_at) AS updated_at
                FROM bugs b
                JOIN projects p ON p.id = b.project_id
                WHERE b.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND (
                        LOWER(b.title) LIKE :keyword
                        OR LOWER(COALESCE(b.description, '')) LIKE :keyword
                  )
                """;
    }

    private String commentSearchSql() {
        return """
                SELECT
                    'COMMENT' AS entity_type,
                    c.id AS entity_id,
                    LEFT(c.content, 120) AS title,
                    c.content AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(c.updated_at, c.created_at) AS updated_at
                FROM task_comments c
                JOIN tasks t ON t.id = c.task_id
                JOIN projects p ON p.id = t.project_id
                WHERE c.deleted_at IS NULL
                  AND t.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND LOWER(c.content) LIKE :keyword
                """;
    }

    private String attachmentSearchSql() {
        return """
                SELECT
                    'ATTACHMENT' AS entity_type,
                    a.id AS entity_id,
                    a.original_file_name AS title,
                    a.content_type AS description,
                    p.id AS project_id,
                    p.code AS project_code,
                    p.name AS project_name,
                    COALESCE(a.updated_at, a.created_at) AS updated_at
                FROM attachments a
                JOIN projects p ON p.id = a.project_id
                WHERE a.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND (
                        LOWER(a.original_file_name) LIKE :keyword
                        OR LOWER(COALESCE(a.content_type, '')) LIKE :keyword
                        OR LOWER(COALESCE(a.extension, '')) LIKE :keyword
                  )
                """;
    }

    private boolean shouldInclude(SearchEntityType selected, SearchEntityType current) {
        return selected == null || selected == current;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private SearchResultViewImpl toView(Object[] row) {
        return new SearchResultViewImpl(
                (String) row[0],
                toUuid(row[1]),
                (String) row[2],
                (String) row[3],
                toUuid(row[4]),
                (String) row[5],
                (String) row[6],
                toInstant(row[7])
        );
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return Instant.parse(value.toString());
    }
}
