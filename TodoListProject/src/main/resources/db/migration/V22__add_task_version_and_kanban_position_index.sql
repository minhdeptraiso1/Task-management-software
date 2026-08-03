ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_tasks_kanban_position
    ON tasks(project_id, current_sprint_id, status, position)
    WHERE deleted_at IS NULL;
