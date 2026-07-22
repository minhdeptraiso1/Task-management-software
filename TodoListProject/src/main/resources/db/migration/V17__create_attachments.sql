CREATE TABLE attachments (
    id UUID PRIMARY KEY,

    project_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,

    uploaded_by_user_id UUID NOT NULL,

    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    extension VARCHAR(50),
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_attachments_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_attachments_uploaded_by
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_attachments_project_entity
    ON attachments(project_id, entity_type, entity_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_attachments_uploaded_by
    ON attachments(uploaded_by_user_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_attachments_project_created_at
    ON attachments(project_id, created_at DESC)
    WHERE deleted_at IS NULL;
