-- Phase 17 / Database review.
-- Không sửa migration cũ: migration này chỉ chuẩn hóa constraint, index và timezone.

-- =====================================================
-- PREFLIGHT: dừng với thông báo rõ ràng nếu dữ liệu cũ không đủ sạch
-- để tạo unique index không phân biệt hoa thường.
-- =====================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        WHERE deleted_at IS NULL
        GROUP BY LOWER(username)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Không thể tạo unique index: username active bị trùng không phân biệt hoa thường';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM users
        WHERE deleted_at IS NULL
        GROUP BY LOWER(email)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Không thể tạo unique index: email active bị trùng không phân biệt hoa thường';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM projects
        WHERE deleted_at IS NULL
        GROUP BY LOWER(code)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Không thể tạo unique index: Project code active bị trùng không phân biệt hoa thường';
    END IF;
END $$;

-- =====================================================
-- FOREIGN KEYS CÒN THIẾU
-- Không dùng ON DELETE CASCADE cho dữ liệu nghiệp vụ.
-- =====================================================

ALTER TABLE token_sessions
    ADD CONSTRAINT fk_token_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE task_dependencies
    ADD CONSTRAINT fk_task_dependencies_task
        FOREIGN KEY (task_id) REFERENCES tasks(id),
    ADD CONSTRAINT fk_task_dependencies_depends_on_task
        FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id);

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_blocked_by_user
        FOREIGN KEY (blocked_by_user_id) REFERENCES users(id);

ALTER TABLE sprint_reviews
    ADD CONSTRAINT fk_sprint_reviews_sprint
        FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    ADD CONSTRAINT fk_sprint_reviews_project
        FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE sprint_retrospectives
    ADD CONSTRAINT fk_sprint_retrospectives_sprint
        FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    ADD CONSTRAINT fk_sprint_retrospectives_project
        FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE notification_recipients
    DROP CONSTRAINT IF EXISTS fk_notification_recipient_notification,
    ADD CONSTRAINT fk_notification_recipient_notification
        FOREIGN KEY (notification_id) REFERENCES notifications(id);

-- =====================================================
-- UNIQUE INDEX TƯƠNG THÍCH SOFT DELETE
-- =====================================================

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_username_key,
    DROP CONSTRAINT IF EXISTS users_email_key;

CREATE UNIQUE INDEX uq_users_username_lower_active
    ON users (LOWER(username))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_users_email_lower_active
    ON users (LOWER(email))
    WHERE deleted_at IS NULL;

ALTER TABLE projects
    DROP CONSTRAINT IF EXISTS uk_projects_code;

CREATE UNIQUE INDEX uq_projects_code_lower_active
    ON projects (LOWER(code))
    WHERE deleted_at IS NULL;

ALTER TABLE notification_recipients
    DROP CONSTRAINT IF EXISTS uk_notification_recipient_user;

CREATE UNIQUE INDEX uq_notification_recipients_notification_user_active
    ON notification_recipients (notification_id, user_id)
    WHERE deleted_at IS NULL;

ALTER TABLE sprint_reviews
    DROP CONSTRAINT IF EXISTS uk_sprint_reviews_sprint;

CREATE UNIQUE INDEX uq_sprint_reviews_sprint_active
    ON sprint_reviews (sprint_id)
    WHERE deleted_at IS NULL;

ALTER TABLE sprint_retrospectives
    DROP CONSTRAINT IF EXISTS uk_sprint_retrospectives_sprint;

CREATE UNIQUE INDEX uq_sprint_retrospectives_sprint_active
    ON sprint_retrospectives (sprint_id)
    WHERE deleted_at IS NULL;

-- =====================================================
-- CHECK CONSTRAINT THEO ENUM VÀ QUY TẮC DỮ LIỆU CƠ BẢN
-- =====================================================

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE'));

ALTER TABLE projects
    ADD CONSTRAINT chk_projects_status
        CHECK (status IN ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED', 'ARCHIVED'));

ALTER TABLE project_members
    ADD CONSTRAINT chk_project_members_role
        CHECK (role IN ('OWNER', 'PROJECT_MANAGER', 'SCRUM_MASTER', 'PRODUCT_OWNER', 'DEVELOPER', 'TESTER', 'VIEWER'));

ALTER TABLE sprints
    ADD CONSTRAINT chk_sprints_status
        CHECK (status IN ('PLANNING', 'ACTIVE', 'COMPLETED', 'CANCELLED'));

ALTER TABLE backlog_items
    ADD CONSTRAINT chk_backlog_items_type
        CHECK (type IN ('EPIC', 'USER_STORY', 'FEATURE', 'TECHNICAL')),
    ADD CONSTRAINT chk_backlog_items_status
        CHECK (status IN ('DRAFT', 'READY', 'IN_SPRINT', 'DONE', 'CANCELLED')),
    ADD CONSTRAINT chk_backlog_items_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    ADD CONSTRAINT chk_backlog_items_position_non_negative
        CHECK (position >= 0);

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_type
        CHECK (type IN ('DEVELOPMENT', 'TESTING', 'DESIGN', 'DOCUMENTATION', 'RESEARCH', 'DEVOPS', 'OTHER')),
    ADD CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED', 'CANCELLED')),
    ADD CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

ALTER TABLE task_time_logs
    ADD CONSTRAINT chk_task_time_logs_minutes_max
        CHECK (minutes <= 720);

ALTER TABLE task_import_batches
    ADD CONSTRAINT chk_task_import_batches_status
        CHECK (status IN ('VALIDATING', 'VALIDATION_FAILED', 'IMPORTING', 'COMPLETED', 'FAILED'));

ALTER TABLE task_dependencies
    ADD CONSTRAINT chk_task_dependencies_not_self
        CHECK (task_id <> depends_on_task_id);

ALTER TABLE bugs
    ADD CONSTRAINT chk_bugs_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    ADD CONSTRAINT chk_bugs_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    ADD CONSTRAINT chk_bugs_status
        CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED', 'REOPENED', 'CLOSED', 'CANCELLED')),
    ADD CONSTRAINT chk_bugs_reopened_count_non_negative
        CHECK (reopened_count >= 0);

ALTER TABLE attachments
    ADD CONSTRAINT chk_attachments_size_positive
        CHECK (size_bytes > 0);

ALTER TABLE bug_attachments
    ADD CONSTRAINT chk_bug_attachments_size_positive
        CHECK (size_bytes > 0);

-- =====================================================
-- PARTIAL/COMPOSITE INDEX KHỚP CÁC API CHÍNH
-- =====================================================

CREATE INDEX idx_projects_status_active
    ON projects (status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_projects_created_at_active
    ON projects (created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_projects_name_lower_active
    ON projects (LOWER(name))
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_members_user_active
    ON project_members (user_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_members_project_active
    ON project_members (project_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_members_project_role_active
    ON project_members (project_id, role)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_sprints_project_status_active
    ON sprints (project_id, status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_sprints_project_created_at_active
    ON sprints (project_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_backlog_items_project_sprint_position_active
    ON backlog_items (project_id, sprint_id, position)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_backlog_items_project_status_active
    ON backlog_items (project_id, status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tasks_project_active
    ON tasks (project_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tasks_project_backlog_active
    ON tasks (project_id, backlog_item_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tasks_assignee_status_due_active
    ON tasks (assignee_user_id, status, due_date)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tasks_project_due_active
    ON tasks (project_id, due_date)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_task_comments_task_created_at_active
    ON task_comments (task_id, created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_task_comments_parent_active
    ON task_comments (parent_comment_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_task_time_logs_task_work_date_active
    ON task_time_logs (task_id, work_date DESC, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_task_time_logs_user_work_date_active
    ON task_time_logs (user_id, work_date DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_notification_recipients_user_read_active_v2
    ON notification_recipients (user_id, read_at)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_notification_recipients_user_delivered_active
    ON notification_recipients (user_id, delivered_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_activity_logs_project_created_at
    ON project_activity_logs (project_id, created_at DESC);

CREATE INDEX idx_project_activity_logs_project_entity
    ON project_activity_logs (project_id, entity_type, entity_id);

CREATE INDEX idx_project_activity_logs_project_action
    ON project_activity_logs (project_id, action);

CREATE INDEX idx_project_activity_logs_performed_by
    ON project_activity_logs (performed_by_user_id, created_at DESC);

CREATE INDEX idx_token_sessions_refresh_hash_active
    ON token_sessions (refresh_token)
    WHERE revoked = FALSE;

CREATE INDEX idx_token_sessions_user_expired_active
    ON token_sessions (user_id, expired_at)
    WHERE revoked = FALSE;

CREATE INDEX idx_task_import_batches_project_created_at_active
    ON task_import_batches (project_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_project_status_active
    ON bugs (project_id, status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_project_severity_active
    ON bugs (project_id, severity)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_project_priority_active
    ON bugs (project_id, priority)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_project_assignee_status_active
    ON bugs (project_id, assignee_user_id, status)
    WHERE deleted_at IS NULL;

-- =====================================================
-- TIMEZONE: các migration cũ dùng TIMESTAMP trong khi entity dùng Instant.
-- Dữ liệu legacy được hiểu là UTC, thống nhất với hibernate.jdbc.time_zone.
-- =====================================================

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC',
    ALTER COLUMN logout_all_at TYPE TIMESTAMP WITH TIME ZONE USING logout_all_at AT TIME ZONE 'UTC';

ALTER TABLE token_sessions
    ALTER COLUMN expired_at TYPE TIMESTAMP WITH TIME ZONE USING expired_at AT TIME ZONE 'UTC';

ALTER TABLE audit_logs
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';

ALTER TABLE projects
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE project_members
    ALTER COLUMN joined_at TYPE TIMESTAMP WITH TIME ZONE USING joined_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE project_activity_logs
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';

ALTER TABLE notifications
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';

ALTER TABLE notification_recipients
    ALTER COLUMN delivered_at TYPE TIMESTAMP WITH TIME ZONE USING delivered_at AT TIME ZONE 'UTC',
    ALTER COLUMN read_at TYPE TIMESTAMP WITH TIME ZONE USING read_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE sprints
    ALTER COLUMN started_at TYPE TIMESTAMP WITH TIME ZONE USING started_at AT TIME ZONE 'UTC',
    ALTER COLUMN completed_at TYPE TIMESTAMP WITH TIME ZONE USING completed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE backlog_items
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE tasks
    ALTER COLUMN completed_at TYPE TIMESTAMP WITH TIME ZONE USING completed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE task_comments
    ALTER COLUMN edited_at TYPE TIMESTAMP WITH TIME ZONE USING edited_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE task_time_logs
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE task_import_batches
    ALTER COLUMN started_at TYPE TIMESTAMP WITH TIME ZONE USING started_at AT TIME ZONE 'UTC',
    ALTER COLUMN completed_at TYPE TIMESTAMP WITH TIME ZONE USING completed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE task_import_errors
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';

ALTER TABLE sprint_reviews
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';

ALTER TABLE sprint_retrospectives
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at AT TIME ZONE 'UTC';
