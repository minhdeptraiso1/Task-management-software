ALTER TABLE bugs
ADD COLUMN sprint_id UUID,
ADD COLUMN due_date DATE,
ADD COLUMN reopened_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE bugs
ADD CONSTRAINT fk_bugs_sprint
FOREIGN KEY (sprint_id)
REFERENCES sprints(id);

CREATE INDEX idx_bugs_sprint_id
    ON bugs(sprint_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_bugs_due_date
    ON bugs(due_date)
    WHERE deleted_at IS NULL;
