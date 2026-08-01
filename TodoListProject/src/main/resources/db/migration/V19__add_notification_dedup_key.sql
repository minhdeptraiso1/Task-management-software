ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS dedup_key VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_notifications_dedup_key
    ON notifications (dedup_key)
    WHERE dedup_key IS NOT NULL;
