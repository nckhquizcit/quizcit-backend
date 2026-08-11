-- This migration is developed and verified against the Docker dev database.
-- Do not run it directly against production; deploy through the reviewed
-- Flyway release process only after a production backup and dry run.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS token_version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_users_active_created_at
    ON users (created_at DESC, user_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_active_username_ci
    ON users (LOWER(BTRIM(username)))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_active_email_ci
    ON users (LOWER(BTRIM(email)))
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS auth_challenges (
    challenge_id        VARCHAR(50) PRIMARY KEY,
    user_id             VARCHAR(50) NOT NULL,
    purpose             VARCHAR(40) NOT NULL,
    target_email        VARCHAR(100),
    otp_hash            VARCHAR(255) NOT NULL,
    failed_attempts     INTEGER NOT NULL DEFAULT 0,
    max_attempts        INTEGER NOT NULL DEFAULT 3,
    expires_at          TIMESTAMPTZ NOT NULL,
    resend_available_at TIMESTAMPTZ NOT NULL,
    verified_at         TIMESTAMPTZ,
    consumed_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_auth_challenges_attempts
        CHECK (failed_attempts >= 0 AND max_attempts > 0),
    CONSTRAINT ck_auth_challenges_purpose
        CHECK (purpose IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET',
                           'EMAIL_CHANGE_OLD', 'EMAIL_CHANGE_NEW'))
);

CREATE INDEX IF NOT EXISTS idx_auth_challenges_user_purpose_created
    ON auth_challenges (user_id, purpose, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_auth_challenges_expiry
    ON auth_challenges (expires_at);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    refresh_token_id VARCHAR(50) PRIMARY KEY,
    user_id          VARCHAR(50) NOT NULL,
    token_hash       VARCHAR(64) NOT NULL UNIQUE,
    token_version    BIGINT NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    revoked_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_active_user
    ON refresh_tokens (user_id, expires_at)
    WHERE revoked_at IS NULL;

CREATE TABLE IF NOT EXISTS auth_rate_limits (
    rate_limit_key VARCHAR(255) NOT NULL,
    action         VARCHAR(50) NOT NULL,
    window_start   TIMESTAMPTZ NOT NULL,
    request_count  INTEGER NOT NULL DEFAULT 0,
    blocked_until  TIMESTAMPTZ,
    PRIMARY KEY (rate_limit_key, action)
);

CREATE TABLE IF NOT EXISTS email_outbox (
    outbox_id      VARCHAR(50) PRIMARY KEY,
    recipient      VARCHAR(100) NOT NULL,
    template       VARCHAR(50) NOT NULL,
    payload        TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    available_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at        TIMESTAMPTZ,
    attempt_count  INTEGER NOT NULL DEFAULT 0,
    last_error     TEXT
);

CREATE INDEX IF NOT EXISTS idx_email_outbox_pending
    ON email_outbox (available_at, created_at)
    WHERE sent_at IS NULL;
