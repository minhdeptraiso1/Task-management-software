CREATE TABLE project_meetings (
 id UUID PRIMARY KEY, project_id UUID NOT NULL REFERENCES projects(id), title VARCHAR(255) NOT NULL,
 description VARCHAR(2000), meeting_type VARCHAR(50) NOT NULL, start_time TIMESTAMP NOT NULL, end_time TIMESTAMP NOT NULL,
 google_meet_link VARCHAR(500), created_by_user_id UUID NOT NULL REFERENCES users(id), created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE, created_by VARCHAR(100), updated_by VARCHAR(100), deleted_at TIMESTAMP WITH TIME ZONE, deleted_by VARCHAR(100),
 CONSTRAINT ck_project_meeting_time CHECK (end_time > start_time),
 CONSTRAINT ck_project_meeting_type CHECK (meeting_type IN ('DAILY','SPRINT_PLANNING','SPRINT_REVIEW','RETROSPECTIVE','ISSUE_RESOLUTION','OTHER'))
);
CREATE INDEX idx_project_meetings_project_start ON project_meetings(project_id,start_time DESC) WHERE deleted_at IS NULL;
