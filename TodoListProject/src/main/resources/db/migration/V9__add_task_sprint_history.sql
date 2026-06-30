ALTER TABLE tasks
    ADD COLUMN origin_sprint_id UUID;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_origin_sprint
        FOREIGN KEY (origin_sprint_id)
            REFERENCES sprints (id)
            ON DELETE SET NULL;

CREATE INDEX idx_tasks_origin_sprint_id
    ON tasks (origin_sprint_id);

UPDATE tasks
SET origin_sprint_id = current_sprint_id
WHERE current_sprint_id IS NOT NULL
  AND origin_sprint_id IS NULL;