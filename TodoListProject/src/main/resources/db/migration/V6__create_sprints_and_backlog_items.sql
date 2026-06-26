-- =====================================================
-- SPRINTS
-- =====================================================

CREATE TABLE sprints
(
    id                   UUID PRIMARY KEY,

    project_id           UUID         NOT NULL,

    name                 VARCHAR(255) NOT NULL,
    goal                 TEXT,

    status               VARCHAR(30)  NOT NULL DEFAULT 'PLANNING',

    start_date           DATE,
    end_date             DATE,

    started_at           TIMESTAMP,
    completed_at         TIMESTAMP,

    created_by_user_id   UUID         NOT NULL,

    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),

    deleted_at           TIMESTAMP,
    deleted_by           VARCHAR(100),

    CONSTRAINT fk_sprints_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id),

    CONSTRAINT fk_sprints_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_sprints_date_range
        CHECK (
            start_date IS NULL
                OR end_date IS NULL
                OR end_date >= start_date
            )
);


-- =====================================================
-- BACKLOG ITEMS
-- =====================================================

CREATE TABLE backlog_items
(
    id                   UUID PRIMARY KEY,

    project_id           UUID         NOT NULL,
    sprint_id            UUID,

    title                VARCHAR(255) NOT NULL,
    description          TEXT,

    type                 VARCHAR(30)  NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    priority             VARCHAR(30)  NOT NULL DEFAULT 'MEDIUM',

    story_points         INTEGER,
    position             BIGINT       NOT NULL DEFAULT 0,

    created_by_user_id   UUID         NOT NULL,

    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),

    deleted_at           TIMESTAMP,
    deleted_by           VARCHAR(100),

    CONSTRAINT fk_backlog_items_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id),

    CONSTRAINT fk_backlog_items_sprint
        FOREIGN KEY (sprint_id)
            REFERENCES sprints (id),

    CONSTRAINT fk_backlog_items_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_backlog_items_story_points
        CHECK (
            story_points IS NULL
                OR story_points >= 0
            )
);


-- =====================================================
-- UNIQUE INDEX
-- =====================================================

/*
 * Không cho trùng tên Sprint đang hoạt động
 * trong cùng một Project.
 *
 * Sprint đã soft delete không còn tham gia unique index.
 */
CREATE UNIQUE INDEX uk_sprints_active_project_name
    ON sprints (project_id, LOWER(name))
    WHERE deleted_at IS NULL;


-- =====================================================
-- SPRINT INDEX
-- =====================================================

CREATE INDEX idx_sprints_project_id
    ON sprints (project_id);

CREATE INDEX idx_sprints_project_status
    ON sprints (project_id, status);

CREATE INDEX idx_sprints_start_end_date
    ON sprints (start_date, end_date);

CREATE INDEX idx_sprints_deleted_at
    ON sprints (deleted_at);


-- =====================================================
-- BACKLOG ITEM INDEX
-- =====================================================

CREATE INDEX idx_backlog_items_project_id
    ON backlog_items (project_id);

CREATE INDEX idx_backlog_items_sprint_id
    ON backlog_items (sprint_id);

CREATE INDEX idx_backlog_items_project_status
    ON backlog_items (project_id, status);

CREATE INDEX idx_backlog_items_project_priority
    ON backlog_items (project_id, priority);

CREATE INDEX idx_backlog_items_project_position
    ON backlog_items (project_id, position);

CREATE INDEX idx_backlog_items_deleted_at
    ON backlog_items (deleted_at);