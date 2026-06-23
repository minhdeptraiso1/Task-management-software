-- User table
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);


-- Token session (refresh token)
CREATE TABLE token_sessions
(
    id            UUID PRIMARY KEY,
    user_id       UUID      NOT NULL,
    refresh_token TEXT      NOT NULL,
    revoked       BOOLEAN   NOT NULL DEFAULT FALSE,
    expired_at    TIMESTAMP NOT NULL
);

-- Audit log
CREATE TABLE audit_logs
(
    id         UUID PRIMARY KEY,
    user_id    UUID,
    action     VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);
