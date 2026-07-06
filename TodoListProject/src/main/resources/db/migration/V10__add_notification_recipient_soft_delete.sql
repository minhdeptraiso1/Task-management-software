-- =====================================================
-- NOTIFICATION RECIPIENT SOFT DELETE
-- =====================================================

ALTER TABLE notification_recipients
    ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE notification_recipients
    ADD COLUMN deleted_by VARCHAR(100);


-- =====================================================
-- INDEX FOR NOTIFICATION QUERY
-- =====================================================

DROP INDEX IF EXISTS idx_notification_recipients_user;

DROP INDEX IF EXISTS idx_notification_recipients_user_read;


CREATE INDEX idx_notification_recipients_user_active
    ON notification_recipients (
                                user_id,
                                deleted_at
        );


CREATE INDEX idx_notification_recipients_user_read_active
    ON notification_recipients (
                                user_id,
                                read_at,
                                deleted_at
        );


CREATE INDEX idx_notification_recipients_notification_user_active
    ON notification_recipients (
                                notification_id,
                                user_id,
                                deleted_at
        );