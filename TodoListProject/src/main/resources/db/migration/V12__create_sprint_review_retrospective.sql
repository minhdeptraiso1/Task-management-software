CREATE TABLE sprint_reviews (
    id UUID PRIMARY KEY,
    sprint_id UUID NOT NULL,
    project_id UUID NOT NULL,
    goal_achieved BOOLEAN,
    demo_summary TEXT,
    stakeholder_feedback TEXT,
    accepted_item_summary TEXT,
    rejected_item_summary TEXT,
    note TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uk_sprint_reviews_sprint UNIQUE (sprint_id)
);

CREATE INDEX idx_sprint_reviews_project_id
    ON sprint_reviews(project_id);

CREATE INDEX idx_sprint_reviews_sprint_id
    ON sprint_reviews(sprint_id);

CREATE TABLE sprint_retrospectives (
    id UUID PRIMARY KEY,
    sprint_id UUID NOT NULL,
    project_id UUID NOT NULL,
    went_well TEXT,
    went_wrong TEXT,
    improvement TEXT,
    action_items_json TEXT,
    note TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uk_sprint_retrospectives_sprint UNIQUE (sprint_id)
);

CREATE INDEX idx_sprint_retrospectives_project_id
    ON sprint_retrospectives(project_id);

CREATE INDEX idx_sprint_retrospectives_sprint_id
    ON sprint_retrospectives(sprint_id);
