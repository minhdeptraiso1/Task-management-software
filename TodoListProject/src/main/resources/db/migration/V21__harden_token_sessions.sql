ALTER TABLE token_sessions
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE token_sessions
    ADD COLUMN IF NOT EXISTS access_token_jti VARCHAR(100);

ALTER TABLE token_sessions
    ADD COLUMN IF NOT EXISTS refresh_token_jti VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_token_sessions_user_active
    ON token_sessions (user_id)
    WHERE revoked = FALSE;

CREATE INDEX IF NOT EXISTS idx_token_sessions_access_jti_active
    ON token_sessions (access_token_jti)
    WHERE revoked = FALSE;
