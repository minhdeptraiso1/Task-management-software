-- Một Project chỉ được có tối đa một Sprint ACTIVE.
CREATE UNIQUE INDEX uk_sprints_one_active_per_project
    ON sprints (project_id)
    WHERE status = 'ACTIVE'
      AND deleted_at IS NULL;