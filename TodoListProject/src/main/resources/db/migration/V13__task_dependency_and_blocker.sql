CREATE TABLE IF NOT EXISTS task_dependencies (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    depends_on_task_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_task_dependencies_task_id
    ON task_dependencies(task_id);

CREATE INDEX IF NOT EXISTS idx_task_dependencies_depends_on_task_id
    ON task_dependencies(depends_on_task_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_task_dependency_active
    ON task_dependencies(task_id, depends_on_task_id)
    WHERE deleted_at IS NULL;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS block_reason TEXT;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS blocked_by_user_id UUID;
