CREATE TABLE bug_comments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    bug_id UUID NOT NULL,
    parent_id UUID,
    author_user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_bug_comments_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_bug_comments_bug FOREIGN KEY (bug_id) REFERENCES bugs(id),
    CONSTRAINT fk_bug_comments_parent FOREIGN KEY (parent_id) REFERENCES bug_comments(id),
    CONSTRAINT fk_bug_comments_author FOREIGN KEY (author_user_id) REFERENCES users(id)
);

CREATE INDEX idx_bug_comments_bug_id ON bug_comments(bug_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_bug_comments_project_bug ON bug_comments(project_id, bug_id) WHERE deleted_at IS NULL;

CREATE TABLE bug_evidences (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    bug_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    steps_to_reproduce TEXT,
    expected_result TEXT,
    actual_result TEXT,
    environment TEXT,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_bug_evidences_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_bug_evidences_bug FOREIGN KEY (bug_id) REFERENCES bugs(id),
    CONSTRAINT fk_bug_evidences_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_bug_evidences_bug_id ON bug_evidences(bug_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_bug_evidences_project_bug ON bug_evidences(project_id, bug_id) WHERE deleted_at IS NULL;

CREATE TABLE bug_attachments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    bug_id UUID NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    original_file_name VARCHAR(500) NOT NULL,
    stored_file_name VARCHAR(500) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_bug_attachments_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_bug_attachments_bug FOREIGN KEY (bug_id) REFERENCES bugs(id),
    CONSTRAINT fk_bug_attachments_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_bug_attachments_bug_id ON bug_attachments(bug_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_bug_attachments_project_bug ON bug_attachments(project_id, bug_id) WHERE deleted_at IS NULL;
