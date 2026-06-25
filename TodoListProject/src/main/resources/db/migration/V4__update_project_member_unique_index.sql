ALTER TABLE project_members
DROP CONSTRAINT IF EXISTS uk_project_members_project_user;

CREATE UNIQUE INDEX uk_project_members_active_project_user
    ON project_members (project_id, user_id)
    WHERE deleted_at IS NULL;