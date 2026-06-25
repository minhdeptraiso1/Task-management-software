-- =====================================================
-- PROJECT ACTIVITY LOG
-- =====================================================
CREATE TABLE project_activity_logs
(
    id                   UUID PRIMARY KEY,
    project_id           UUID         NOT NULL,
    entity_type          VARCHAR(50)  NOT NULL,
    entity_id            UUID,
    action               VARCHAR(100) NOT NULL,
    performed_by_user_id UUID         NOT NULL,
    old_value_json       TEXT,
    new_value_json       TEXT,
    created_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_project_activity_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_activity_user FOREIGN KEY (performed_by_user_id) REFERENCES users (id)
);
-- =====================================================
-- NOTIFICATION
-- =====================================================
CREATE TABLE notifications
(
    id            UUID PRIMARY KEY,
    type          VARCHAR(100) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    actor_user_id UUID,
    project_id    UUID,
    entity_type   VARCHAR(50),
    entity_id     UUID,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_user_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_project FOREIGN KEY (project_id) REFERENCES projects (id)
);
-- =====================================================
-- NOTIFICATION RECIPIENT
-- =====================================================
CREATE TABLE notification_recipients
(
    id              UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    user_id         UUID NOT NULL,
    delivered_at    TIMESTAMP,
    read_at         TIMESTAMP,
    CONSTRAINT fk_notification_recipient_notification FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_recipient_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_notification_recipient_user UNIQUE (notification_id, user_id)
);
-- =====================================================
-- INDEX
-- =====================================================
CREATE INDEX idx_project_activity_project ON project_activity_logs (project_id);
CREATE INDEX idx_project_activity_entity ON project_activity_logs (entity_type, entity_id);
CREATE INDEX idx_project_activity_created_at ON project_activity_logs (created_at);
CREATE INDEX idx_notifications_project ON notifications (project_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
CREATE INDEX idx_notification_recipients_user ON notification_recipients (user_id);
CREATE INDEX idx_notification_recipients_user_read ON notification_recipients (user_id, read_at);