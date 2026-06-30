-- =====================================================
-- TASKS
-- =====================================================

CREATE TABLE tasks
(
    id                UUID PRIMARY KEY,

    project_id        UUID         NOT NULL,
    backlog_item_id   UUID         NOT NULL,

    /*
     * Sprint hiện tại của Task.
     *
     * Task vẫn tồn tại khi Sprint bị hủy.
     * Khi Backlog Item được đưa về Product Backlog,
     * current_sprint_id của Task chưa hoàn thành sẽ về null.
     */
    current_sprint_id UUID,

    title             VARCHAR(255) NOT NULL,
    description       TEXT,

    type              VARCHAR(30)  NOT NULL DEFAULT 'DEVELOPMENT',
    status            VARCHAR(30)  NOT NULL DEFAULT 'TODO',
    priority          VARCHAR(30)  NOT NULL DEFAULT 'MEDIUM',

    assignee_user_id  UUID,
    reporter_user_id  UUID         NOT NULL,

    estimated_minutes INTEGER,
    start_date        DATE,
    due_date          DATE,
    completed_at      TIMESTAMP,

    /*
     * Vị trí trong một cột Kanban.
     */
    position          BIGINT       NOT NULL DEFAULT 1,

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),

    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(100),

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id),

    CONSTRAINT fk_tasks_backlog_item
        FOREIGN KEY (backlog_item_id)
            REFERENCES backlog_items (id),

    /*
     * Không dùng ON DELETE CASCADE.
     * Nếu Sprint bị xóa vật lý trong tương lai,
     * Task vẫn còn và current_sprint_id được đưa về null.
     */
    CONSTRAINT fk_tasks_current_sprint
        FOREIGN KEY (current_sprint_id)
            REFERENCES sprints (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_user_id)
            REFERENCES users (id),

    CONSTRAINT fk_tasks_reporter
        FOREIGN KEY (reporter_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_tasks_estimated_minutes
        CHECK (
            estimated_minutes IS NULL
                OR estimated_minutes >= 0
            ),

    CONSTRAINT chk_tasks_date_range
        CHECK (
            start_date IS NULL
                OR due_date IS NULL
                OR due_date >= start_date
            ),

    CONSTRAINT chk_tasks_position
        CHECK (position >= 1)
);


-- =====================================================
-- TASK COMMENTS
-- =====================================================

CREATE TABLE task_comments
(
    id                UUID PRIMARY KEY,

    task_id           UUID NOT NULL,
    user_id           UUID NOT NULL,

    /*
     * Hỗ trợ reply comment.
     */
    parent_comment_id UUID,

    content           TEXT NOT NULL,
    edited_at         TIMESTAMP,

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),

    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(100),

    CONSTRAINT fk_task_comments_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id),

    CONSTRAINT fk_task_comments_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_task_comments_parent
        FOREIGN KEY (parent_comment_id)
            REFERENCES task_comments (id)
            ON DELETE SET NULL
);


-- =====================================================
-- TASK TIME LOGS
-- =====================================================

CREATE TABLE task_time_logs
(
    id          UUID PRIMARY KEY,

    task_id     UUID    NOT NULL,
    user_id     UUID    NOT NULL,

    work_date   DATE    NOT NULL,
    minutes     INTEGER NOT NULL,
    description TEXT,

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),

    CONSTRAINT fk_task_time_logs_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id),

    CONSTRAINT fk_task_time_logs_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT chk_task_time_logs_minutes
        CHECK (minutes > 0)
);


-- =====================================================
-- TASK IMPORT BATCHES
-- =====================================================

CREATE TABLE task_import_batches
(
    id                  UUID PRIMARY KEY,

    project_id          UUID         NOT NULL,
    sprint_id           UUID         NOT NULL,
    imported_by_user_id UUID         NOT NULL,

    original_file_name  VARCHAR(500) NOT NULL,

    status              VARCHAR(40)  NOT NULL,

    total_rows          INTEGER      NOT NULL DEFAULT 0,
    success_rows        INTEGER      NOT NULL DEFAULT 0,
    failed_rows         INTEGER      NOT NULL DEFAULT 0,

    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,

    error_message       TEXT,

    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),

    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(100),

    CONSTRAINT fk_task_import_batches_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id),

    CONSTRAINT fk_task_import_batches_sprint
        FOREIGN KEY (sprint_id)
            REFERENCES sprints (id),

    CONSTRAINT fk_task_import_batches_user
        FOREIGN KEY (imported_by_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_task_import_batches_rows
        CHECK (
            total_rows >= 0
                AND success_rows >= 0
                AND failed_rows >= 0
            )
);


-- =====================================================
-- TASK IMPORT ERRORS
-- =====================================================

CREATE TABLE task_import_errors
(
    id              UUID PRIMARY KEY,

    import_batch_id UUID      NOT NULL,

    row_number      INTEGER   NOT NULL,
    field_name      VARCHAR(100),
    raw_value       TEXT,
    error_message   TEXT      NOT NULL,

    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_import_errors_batch
        FOREIGN KEY (import_batch_id)
            REFERENCES task_import_batches (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_task_import_errors_row
        CHECK (row_number >= 1)
);


-- =====================================================
-- TASK INDEXES
-- =====================================================

CREATE INDEX idx_tasks_project_id
    ON tasks (project_id);

CREATE INDEX idx_tasks_backlog_item_id
    ON tasks (backlog_item_id);

CREATE INDEX idx_tasks_current_sprint_id
    ON tasks (current_sprint_id);

CREATE INDEX idx_tasks_assignee_user_id
    ON tasks (assignee_user_id);

CREATE INDEX idx_tasks_project_status
    ON tasks (project_id, status);

CREATE INDEX idx_tasks_sprint_status
    ON tasks (current_sprint_id, status);

CREATE INDEX idx_tasks_kanban_position
    ON tasks (
              project_id,
              current_sprint_id,
              status,
              position
        );

CREATE INDEX idx_tasks_due_date
    ON tasks (due_date);

CREATE INDEX idx_tasks_deleted_at
    ON tasks (deleted_at);


-- =====================================================
-- COMMENT INDEXES
-- =====================================================

CREATE INDEX idx_task_comments_task_id
    ON task_comments (task_id);

CREATE INDEX idx_task_comments_user_id
    ON task_comments (user_id);

CREATE INDEX idx_task_comments_parent_id
    ON task_comments (parent_comment_id);

CREATE INDEX idx_task_comments_created_at
    ON task_comments (task_id, created_at);

CREATE INDEX idx_task_comments_deleted_at
    ON task_comments (deleted_at);


-- =====================================================
-- TIME LOG INDEXES
-- =====================================================

CREATE INDEX idx_task_time_logs_task_id
    ON task_time_logs (task_id);

CREATE INDEX idx_task_time_logs_user_id
    ON task_time_logs (user_id);

CREATE INDEX idx_task_time_logs_work_date
    ON task_time_logs (work_date);

CREATE INDEX idx_task_time_logs_task_work_date
    ON task_time_logs (task_id, work_date);

CREATE INDEX idx_task_time_logs_deleted_at
    ON task_time_logs (deleted_at);


-- =====================================================
-- IMPORT INDEXES
-- =====================================================

CREATE INDEX idx_task_import_batches_project_id
    ON task_import_batches (project_id);

CREATE INDEX idx_task_import_batches_sprint_id
    ON task_import_batches (sprint_id);

CREATE INDEX idx_task_import_batches_user_id
    ON task_import_batches (imported_by_user_id);

CREATE INDEX idx_task_import_batches_status
    ON task_import_batches (status);

CREATE INDEX idx_task_import_errors_batch_id
    ON task_import_errors (import_batch_id);

CREATE INDEX idx_task_import_errors_row_number
    ON task_import_errors (
                           import_batch_id,
                           row_number
        );