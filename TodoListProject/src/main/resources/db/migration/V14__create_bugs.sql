CREATE TABLE bugs (
    id UUID PRIMARY KEY,

    project_id UUID NOT NULL,
    backlog_item_id UUID,
    task_id UUID,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    severity VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    assignee_user_id UUID,
    reporter_user_id UUID NOT NULL,

    reproduction_steps TEXT,
    expected_result TEXT,
    actual_result TEXT,

    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_bugs_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_bugs_backlog_item
        FOREIGN KEY (backlog_item_id)
        REFERENCES backlog_items(id),

    CONSTRAINT fk_bugs_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id),

    CONSTRAINT fk_bugs_assignee
        FOREIGN KEY (assignee_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_bugs_reporter
        FOREIGN KEY (reporter_user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_bugs_project_id
    ON bugs(project_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_task_id
    ON bugs(task_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_backlog_item_id
    ON bugs(backlog_item_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_assignee_user_id
    ON bugs(assignee_user_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_status
    ON bugs(status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_severity
    ON bugs(severity)
    WHERE deleted_at IS NULL;
