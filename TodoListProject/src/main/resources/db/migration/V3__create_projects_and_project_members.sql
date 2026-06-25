-- =====================================================
-- PROJECT
-- =====================================================

CREATE TABLE projects
(
    id                 UUID PRIMARY KEY,
    code               VARCHAR(50)  NOT NULL,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    status             VARCHAR(30)  NOT NULL DEFAULT 'PLANNING',

    start_date         DATE,
    end_date           DATE,

    created_by_user_id UUID         NOT NULL,

    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),

    deleted_at         TIMESTAMP,
    deleted_by         VARCHAR(100),

    CONSTRAINT uk_projects_code
        UNIQUE (code),

    CONSTRAINT fk_projects_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_projects_date
        CHECK (
            start_date IS NULL
                OR end_date IS NULL
                OR end_date >= start_date
            )
);


-- =====================================================
-- PROJECT MEMBER
-- =====================================================

CREATE TABLE project_members
(
    id         UUID PRIMARY KEY,

    project_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,

    role       VARCHAR(30) NOT NULL,
    joined_at  TIMESTAMP   NOT NULL,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    CONSTRAINT uk_project_members_project_user
        UNIQUE (project_id, user_id),

    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id),

    CONSTRAINT fk_project_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);


-- =====================================================
-- INDEX
-- =====================================================

CREATE INDEX idx_projects_status
    ON projects (status);

CREATE INDEX idx_projects_created_by_user_id
    ON projects (created_by_user_id);

CREATE INDEX idx_projects_deleted_at
    ON projects (deleted_at);


CREATE INDEX idx_project_members_project_id
    ON project_members (project_id);

CREATE INDEX idx_project_members_user_id
    ON project_members (user_id);

CREATE INDEX idx_project_members_role
    ON project_members (role);

CREATE INDEX idx_project_members_deleted_at
    ON project_members (deleted_at);