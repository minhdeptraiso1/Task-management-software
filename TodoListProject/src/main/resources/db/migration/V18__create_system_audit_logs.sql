CREATE TABLE system_audit_logs (
    id UUID PRIMARY KEY,

    actor_user_id UUID,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,

    ip_address VARCHAR(100),
    user_agent VARCHAR(1000),

    old_value_json TEXT,
    new_value_json TEXT,

    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(2000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_system_audit_logs_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_system_audit_logs_actor
    ON system_audit_logs(actor_user_id, created_at DESC);

CREATE INDEX idx_system_audit_logs_action
    ON system_audit_logs(action, created_at DESC);

CREATE INDEX idx_system_audit_logs_resource
    ON system_audit_logs(resource_type, resource_id, created_at DESC);

CREATE INDEX idx_system_audit_logs_created_at
    ON system_audit_logs(created_at DESC);
