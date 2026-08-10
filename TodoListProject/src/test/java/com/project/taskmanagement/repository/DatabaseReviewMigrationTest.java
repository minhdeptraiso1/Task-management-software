package com.project.taskmanagement.repository;

import com.project.taskmanagement.repository.support.RepositoryTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseReviewMigrationTest extends RepositoryTestBase {

    private static final List<String> REVIEW_FOREIGN_KEYS = List.of(
            "fk_token_sessions_user",
            "fk_audit_logs_user",
            "fk_task_dependencies_task",
            "fk_task_dependencies_depends_on_task",
            "fk_tasks_blocked_by_user",
            "fk_sprint_reviews_sprint",
            "fk_sprint_reviews_project",
            "fk_sprint_retrospectives_sprint",
            "fk_sprint_retrospectives_project"
    );

    private static final List<String> REVIEW_INDEXES = List.of(
            "uq_users_username_lower_active",
            "uq_users_email_lower_active",
            "uq_projects_code_lower_active",
            "uq_notification_recipients_notification_user_active",
            "idx_sprints_project_status_active",
            "idx_backlog_items_project_sprint_position_active",
            "idx_tasks_assignee_status_due_active",
            "idx_task_time_logs_user_work_date_active",
            "idx_project_activity_logs_project_created_at",
            "idx_token_sessions_refresh_hash_active"
    );

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void migrationShouldCreateRequiredForeignKeysAndIndexes() {
        String foreignKeyPlaceholders = String.join(
                ", ",
                java.util.Collections.nCopies(REVIEW_FOREIGN_KEYS.size(), "?")
        );
        String indexPlaceholders = String.join(
                ", ",
                java.util.Collections.nCopies(REVIEW_INDEXES.size(), "?")
        );
        Integer foreignKeyCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM pg_constraint
                        WHERE contype = 'f'
                          AND conname IN (%s)
                        """.formatted(foreignKeyPlaceholders),
                Integer.class,
                REVIEW_FOREIGN_KEYS.toArray()
        );
        Integer indexCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM pg_indexes
                        WHERE schemaname = current_schema()
                          AND indexname IN (%s)
                        """.formatted(indexPlaceholders),
                Integer.class,
                REVIEW_INDEXES.toArray()
        );

        assertThat(foreignKeyCount).isEqualTo(REVIEW_FOREIGN_KEYS.size());
        assertThat(indexCount).isEqualTo(REVIEW_INDEXES.size());
    }

    @Test
    void instantColumnsShouldUseTimestampWithTimeZone() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = current_schema()
                          AND data_type = 'timestamp with time zone'
                          AND (table_name, column_name) IN (
                              ('users', 'created_at'),
                              ('users', 'logout_all_at'),
                              ('projects', 'created_at'),
                              ('project_members', 'joined_at'),
                              ('sprints', 'started_at'),
                              ('tasks', 'completed_at'),
                              ('task_comments', 'edited_at'),
                              ('task_import_batches', 'started_at')
                          )
                        """,
                Integer.class
        );

        assertThat(count).isEqualTo(8);
    }

    @Test
    void userUniquenessShouldIgnoreCaseAndExcludeSoftDeletedRows() {
        UUID deletedUserId = UUID.randomUUID();
        UUID activeUserId = UUID.randomUUID();

        insertUser(deletedUserId, "Database.Review", "database.review@example.com");
        jdbcTemplate.update(
                "UPDATE users SET deleted_at = now(), deleted_by = 'test' WHERE id = ?",
                deletedUserId
        );

        insertUser(activeUserId, "database.review", "DATABASE.REVIEW@example.com");

        assertThatThrownBy(() -> insertUser(
                UUID.randomUUID(),
                "DATABASE.REVIEW",
                "database.review@example.com"
        )).hasMessageContaining("uq_users_username_lower_active");
    }

    private void insertUser(UUID id, String username, String email) {
        jdbcTemplate.update(
                """
                        INSERT INTO users (
                            id, username, email, password, role, enabled,
                            created_at, created_by
                        ) VALUES (?, ?, ?, 'test-password', 'EMPLOYEE', TRUE, now(), 'test')
                        """,
                id,
                username,
                email
        );
    }
}
