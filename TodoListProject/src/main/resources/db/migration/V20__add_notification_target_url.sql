ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS target_url VARCHAR(500);
