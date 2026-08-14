CREATE TABLE ai_request_logs (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    requested_by_user_id UUID NOT NULL REFERENCES users(id),
    request_type VARCHAR(50) NOT NULL,
    prompt TEXT NOT NULL,
    response TEXT,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(100),
    CONSTRAINT ck_ai_request_type CHECK (request_type IN ('PROJECT_QA','MEETING_SUGGESTION','MEETING_MINUTES','ACTION_ITEMS')),
    CONSTRAINT ck_ai_prompt_length CHECK (char_length(prompt) BETWEEN 1 AND 2000)
);
CREATE INDEX idx_ai_request_logs_project_created ON ai_request_logs(project_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_ai_request_logs_user_created ON ai_request_logs(requested_by_user_id, created_at DESC) WHERE deleted_at IS NULL;
